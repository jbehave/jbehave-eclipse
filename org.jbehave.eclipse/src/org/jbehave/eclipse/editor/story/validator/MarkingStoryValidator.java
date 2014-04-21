package org.jbehave.eclipse.editor.story.validator;

import static fj.data.List.iterableList;

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
import org.jbehave.eclipse.editor.story.Marks;
import org.jbehave.eclipse.editor.text.MarkData;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.util.MonitoredExecutor;
import org.jbehave.eclipse.util.Strings;
import org.jbehave.eclipse.util.Visitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Equal;
import fj.F;
import fj.Ord;

public class MarkingStoryValidator extends AbstractStoryValidator {
    public static final String MARKER_ID = Activator.PLUGIN_ID + ".storyMarker";
    
    private static Logger log = LoggerFactory.getLogger(MarkingStoryValidator.class);

    private IFile file;

    private boolean applyMarkAsynchronously;

    public MarkingStoryValidator(JBehaveProject project, IFile file, IDocument document) {
        super(project, document);
        this.file = file;
    }

    public void removeExistingMarkers() {
        try {
            file.deleteMarkers(MARKER_ID, true, IResource.DEPTH_ZERO);
        } catch (CoreException e) {
            log.error("Failed to delete existing marks", e);
        }
    }

    protected void analyze(final List<StoryElement> storyElements, final Runnable afterApplyCallback) {
        final fj.data.List<Part> parts = iterableList(storyElements).map(new F<StoryElement,Part>() {
            @Override
            public Part f(StoryElement storyElement) {
                return new Part(storyElement);
            }
        });
        
        Activator.logInfo(MarkingStoryValidator.class.getSimpleName()+": Validating parts " + parts);
        
        MonitoredExecutor executor = new MonitoredExecutor(Activator.getDefault().getExecutor());
        executor.execute(checkStepsAsRunnable(parts));
        executor.execute(checkNarrativeAsRunnable(parts));

        try {
            Activator.logInfo(MarkingStoryValidator.class.getSimpleName()+": Awaiting termination of validation");
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Error while validating parts: " + parts, e);
        }

        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                monitor.beginTask("Applying marks", parts.length());
                for (Part part : parts) {
                    applyMarks(part.getStoryElement(), part.getMarks());
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

    public void applyMarks(StoryElement storyElement, ConcurrentLinkedQueue<MarkData> marks) {
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
            Activator.logError(MarkingStoryValidator.class.getSimpleName(), e);
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
        Part soThat = null;
        
        Iterator<Part> iterator = parts.iterator();
        while(iterator.hasNext()) {
            Part part = iterator.next();
            Keyword keyword = part.partType();
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
                            else if(asA!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleAsA, "Only one 'As a' element is allowed");
                            else
                                asA = part;
                            break;
                        case IWantTo:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(asA==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element");
                            else if(iWantTo!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleIWantTo, "Only one 'I want to' element is allowed");
                            else
                                iWantTo = part;
                            break;
                        case SoThat:
                            if(narrative==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element");
                            else if(asA==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element");
                            else if(iWantTo==null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to' element");
                            else if(soThat!=null)
                                part.addErrorMark(Marks.Code.InvalidNarrativeSequence_multipleSoThat, "Only one 'So that' element is allowed");
                            else
                                soThat = part;
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
            } else if (soThat!=null){
                if(asA!=null) {
                    if(iWantTo==null) {
                      asA.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to' element");
                    }
                }
                else {
                	soThat.addErrorMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element");
                }
            }
        }
        
    }

    protected void checkSteps(final fj.data.List<Part> parts) throws JavaModelException {
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
    
}
