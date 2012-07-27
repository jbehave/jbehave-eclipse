package org.jbehave.eclipse.editor.story;

import static fj.data.List.iterableList;
import static org.jbehave.eclipse.util.Objects.o;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.IDocument;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.step.StepLocator;
import org.jbehave.eclipse.editor.step.TransformByPriority;
import org.jbehave.eclipse.editor.text.MarkData;
import org.jbehave.eclipse.parser.RegexUtils;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.util.New;
import org.jbehave.eclipse.util.ProcessGroup;
import org.jbehave.eclipse.util.Strings;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Equal;
import fj.F;
import fj.Ord;

public class MarkingStoryValidator {
    public static final String MARKER_ID = Activator.PLUGIN_ID + ".storyMarker";
    
    private static Logger log = LoggerFactory.getLogger(MarkingStoryValidator.class);

    private IFile file;
    private IDocument document;
    private JBehaveProject project;

    private boolean applyMarkAsynchronously;

    public MarkingStoryValidator(JBehaveProject project, IFile file, IDocument document) {
        this.project = project;
        this.file = file;
        this.document = document;
    }

    public void removeExistingMarkers() {
        try {
            file.deleteMarkers(MARKER_ID, true, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            log.error("Failed to delete existing marks", e);
        }
    }

    public void validate(Runnable afterApplyCallback) {
        analyze(new StoryDocumentUtils(project.getLocalizedStepSupport()).getStoryElements(document), afterApplyCallback);
    }

    private void analyze(final List<StoryElement> storyElements, final Runnable afterApplyCallback) {
        final fj.data.List<Part> parts = iterableList(storyElements).map(new F<StoryElement,Part>() {
            @Override
            public Part f(StoryElement storyElement) {
                return new Part(storyElement);
            }
        });
        
        Activator.logInfo(MarkingStoryValidator.class.getSimpleName()+": Validating parts " + parts);
        
        ProcessGroup<?> group = Activator.getDefault().newProcessGroup();
        group.spawn(checkStepsAsRunnable(parts));
        group.spawn(checkNarrativeAsRunnable(parts));

        try {
            Activator.logInfo(MarkingStoryValidator.class.getSimpleName()+": Awaiting termination of validation");
            group.awaitTermination();
        } catch (InterruptedException e) {
            Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Error while validating parts: " + parts, e);
        }

        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                monitor.beginTask("Applying marks", parts.length());
                for (Part part : parts) {
                    part.applyMarks();
                    monitor.worked(1);
                }
                afterApplyCallback.run();
            }
        };
        try {
            if(applyMarkAsynchronously)
                file.getWorkspace().run(r, null, IWorkspace.AVOID_UPDATE, null);
            else
                r.run(new NullProgressMonitor());
        } catch (CoreException e) {
            Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Error while applying marks on file <" + file + ">", e);
        }
    }
    
    private Runnable checkNarrativeAsRunnable(final fj.data.List<Part> parts) {
        return new Runnable() {
            public void run() {
                try {
                    checkNarrative(parts);
                } catch (Throwable e) {
                    Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Error while checking narrative for parts: " + parts, e);
                }
            }
        };
    }
    
    private void checkNarrative(final fj.data.List<Part> parts) throws JavaModelException {
        boolean nonNarrativeOrIgnorable = false;
        
        Part narrative = null;
        Part inOrderTo = null;
        Part asA = null;
        Part iWantTo = null;
        
        Iterator<Part> iterator = parts.iterator();
        while(iterator.hasNext()) {
            Part part = iterator.next();
            Keyword keyword = part.storyElement.getPreferredKeyword();
            if(keyword==null) {
                continue;
            }
            if(keyword.isNarrative()) {
                // narrative must be the first
                if(nonNarrativeOrIgnorable) {
                    part.addErrorMark(Marks.Code.InvalidNarrativePosition, "Narrative must be the first section");
                }
                else {
                    switch(keyword) {
                        case Narrative:
                            if(narrative!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleNarrative, "Only one 'Narrative:' element is allowed");
                            else
                                narrative = part;
                            break;
                        case InOrderTo:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleInOrderTo, "Only one 'In order to' element is allowed");
                            else
                                inOrderTo = part;
                            break;
                        case AsA:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to' element");
                            else if(asA!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleAsA, "Only one 'As a' element is allowed");
                            else
                                asA = part;
                            break;
                        case IWantTo:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(inOrderTo==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to' element");
                            else if(asA==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element");
                            else if(iWantTo!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleIWantTo, "Only one 'I want to' element is allowed");
                            else
                                iWantTo = part;
                            break;
					default:
						break;
                    }
                }
            }
            else if( keyword != Keyword.Ignorable
                  && keyword != Keyword.Meta
                  && keyword != Keyword.MetaProperty
                  && keyword != Keyword.GivenStories) {
                nonNarrativeOrIgnorable = true;
            }
        }
        
        // consolidation
        if(narrative!=null) {
            if(inOrderTo!=null) {
                if(asA!=null) {
                    if(iWantTo==null) {
                      asA.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to' element");
                    }
                }
                else {
                    inOrderTo.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element");
                }
            }
            else {
                narrative.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingInOrderTo, "Missing 'In order to' element");
            }
        }
        
    }
    
    private Runnable checkStepsAsRunnable(final fj.data.List<Part> parts)  {
        return new Runnable() {
            public void run() {
                try {
                    checkSteps(parts);
                } catch (Throwable e) {
                    Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Error while checking steps for parts: " + parts, e);
                }
            }
        };
    }

    private void checkSteps(final fj.data.List<Part> parts) throws JavaModelException {
        final fj.data.List<Part> steps = parts.filter(new F<Part,Boolean>() {
            public Boolean f(Part part) {
                return Keyword.isStep(part.partType());
            };
        });
        
        log.debug("Validating steps");
        
        StepLocator locator = project.getStepLocator();
        locator.traverseSteps(new Visitor<StepCandidate, Object>() {
            @Override
            public void visit(StepCandidate candidate) {
                log.debug("Evaluating candidate: <{}>", candidate);
                for (Part part : steps) {
                    part.evaluateCandidate(candidate);
                }
            }
        });
        
        log.debug("All candidates have been evaluated");

        for (Part part : steps) {
            String key = part.extractStepSentenceAndRemoveTrailingNewlines();
            ConcurrentLinkedQueue<StepCandidate> candidates = part.getCandidates();
            int count = candidates.size();
            log.debug("#" + count + "result(s) found for >>" + Strings.escapeNL(key) + "<<");
            if (count == 0)
                part.addWarningMark(Marks.Code.NoMatchingStep, "No step is matching <" + key + ">");
            else if (count > 1) {
                
                fj.data.List<Integer> collectedPrios = iterableList(candidates).map(new TransformByPriority());
                int max = collectedPrios.maximum(Ord.intOrd);
                int countWithMax = collectedPrios.filter(Equal.intEqual.eq(max)).length();
                if (countWithMax>1) {
                    MarkData mark = part.addWarningMark(Marks.Code.MultipleMatchingSteps, "Ambiguous step: " + count + " steps are matching <" + key + "> got: " + candidates);
                    Marks.putStepsAsHtml(mark, candidates);
                }
                else {
                    MarkData mark = part.addInfoMark(Marks.Code.MultipleMatchingSteps_PrioritySelection, 
                            "Multiple steps matching, but only one with the highest priority for <" + key + ">");
                    Marks.putStepsAsHtml(mark, candidates);
                    log.debug("#{} matching steps but only one with the highest priority for {}", candidates.size(), key);
                }
            }
        }
    }
    
    class Part {
        private final ConcurrentLinkedQueue<MarkData> marks = New.concurrentLinkedQueue();
        private final ConcurrentLinkedQueue<StepCandidate> candidates = New.concurrentLinkedQueue();
        private final StoryElement storyElement;

        private Part(StoryElement storyElement) {
            super();
            this.storyElement = storyElement;
            computeExtractStepSentenceAndRemoveTrailingNewlines();
        }
        
        public void evaluateCandidate(StepCandidate candidate) {
            String pattern = extractStepSentenceAndRemoveTrailingNewlines();
            Keyword type = partType();
            log.debug("Candidate evaluated against part {}", storyElement);
            boolean patternMatch = candidate.matches(pattern);
            boolean typeMatch = type.isSameAs(candidate.stepType);
            if (patternMatch && typeMatch) {
                addCandidate(candidate);
                log.debug("<{} {}> accepts <{} {}>", o(type, pattern, //
                        candidate.stepType, candidate.stepPattern));
            }
            else {
                log.debug("<{} {}> rejects <{} {}>", o(type, pattern, //
                        candidate.stepType, candidate.stepPattern));
            }
        }
        
        public ConcurrentLinkedQueue<StepCandidate> getCandidates() {
            return candidates;
        }

        private void addCandidate(StepCandidate candidate) {
            candidates.add(candidate);
        }

        private Keyword partType() {
            return storyElement.getPreferredKeyword();
        }

        private String extractStepSentenceAndRemoveTrailingNewlines;
        public String extractStepSentenceAndRemoveTrailingNewlines() {
            return extractStepSentenceAndRemoveTrailingNewlines;
        }
        
        private void computeExtractStepSentenceAndRemoveTrailingNewlines() {
            String stepSentence = storyElement.stepWithoutKeyword();
            // remove any comment that can still be within the step
            String cleaned = RegexUtils.removeComment(stepSentence);
            extractStepSentenceAndRemoveTrailingNewlines = Strings.removeTrailingNewlines(cleaned);
        }

        public MarkData addErrorMark(Marks.Code code, String message) {
            return addMark(code, message, IMarker.SEVERITY_ERROR);
        }
        
        public MarkData addInfoMark(Marks.Code code, String message) {
            return addMark(code, message, IMarker.SEVERITY_INFO);
        }

        public MarkData addWarningMark(Marks.Code code, String message) {
            return addMark(code, message, IMarker.SEVERITY_WARNING);
        }

        public MarkData addMark(Marks.Code code, String message, int severity) {
            MarkData markData = new MarkData()//
                    .severity(severity)//
                    .message(message)//
                    .offsetStart(storyElement.getOffsetStart())//
                    .offsetEnd(storyElement.getOffsetEnd());
            Marks.putCode(markData, code);
            marks.add(markData);
            return markData;
        }

        public void applyMarks() {
            if (marks.isEmpty())
                return;

            try {
                for (MarkData mark : marks) {
                    IMarker marker = file.createMarker(MARKER_ID);
                    marker.setAttributes(mark.createAttributes(file, document));
                    Keyword keyword = storyElement.getPreferredKeyword();
                    if(keyword!=null)
                        marker.setAttribute("Keyword", keyword.name());
                }
            } catch (Exception e) {
                Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Failed to apply marks", e);
            }
        }

        public String text() {
            return storyElement.getContent();
        }

        public String textWithoutTrailingNewlines() {
            return Strings.removeTrailingNewlines(text());
        }

        @Override
        public String toString() {
            return "Part [offset=" + storyElement.getOffset() + ", length=" + storyElement.getLength() + ", keyword=" + storyElement.getPreferredKeyword() + ", marks="
                    + marks + ", text=" + textWithoutTrailingNewlines() + "]";
        }

    }

}
