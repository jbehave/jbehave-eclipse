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

    protected void validate(final List<StoryElement> storyElements) {
        final fj.data.List<ValidatingPart> parts = iterableList(storyElements).map(new F<StoryElement,ValidatingPart>() {
            @Override
            public ValidatingPart f(StoryElement storyElement) {
                return new ValidatingPart(storyElement);
            }
        });
        
        Activator.logInfo(MarkingStoryValidator.class.getSimpleName()+": Validating: " + parts);
        
        MonitoredExecutor executor = new MonitoredExecutor(Activator.getDefault().getExecutor());
        executor.execute(validateStepsAsRunnable(parts));
        executor.execute(validateNarrativeAsRunnable(parts));

        try {
            Activator.logInfo(MarkingStoryValidator.class.getSimpleName()+": Awaiting termination of validation");
            executor.awaitCompletion();
        } catch (InterruptedException e) {
            Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Error while validating: " + parts, e);
        }

        IWorkspaceRunnable r = new IWorkspaceRunnable() {
            public void run(IProgressMonitor monitor) throws CoreException {
                monitor.beginTask("Applying marks", parts.length());
                for (ValidatingPart part : parts) {
                    applyMarks(part.getStoryElement(), part.getMarks());
                    monitor.worked(1);
                }
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

    private Runnable validateNarrativeAsRunnable(final fj.data.List<ValidatingPart> parts) {
        return new Runnable() {
            public void run() {
                try {
                    validateNarrative(parts);
                } catch (Throwable e) {
                    Activator.logError(MarkingStoryValidator.class.getSimpleName()+": Error while validating narrative: " + parts, e);
                }
            }
        };
    }
    
    private void validateNarrative(final fj.data.List<ValidatingPart> parts) throws JavaModelException {
        boolean nonNarrativeOrIgnorable = false;
        
        ValidatingPart narrative = null;
        ValidatingPart inOrderTo = null;
        ValidatingPart asA = null;
        ValidatingPart iWantTo = null;
        ValidatingPart soThat = null;
        
        Iterator<ValidatingPart> iterator = parts.iterator();
        while(iterator.hasNext()) {
            ValidatingPart part = iterator.next();
            Keyword keyword = part.getKeyword();
            if(keyword==null) {
                continue;
            }
            if(keyword.isNarrative()) {
                // narrative must be the first
                if(nonNarrativeOrIgnorable) {
                    part.addMark(Marks.Code.InvalidNarrativePosition, "Narrative must be the first section", IMarker.SEVERITY_ERROR);
                }
                else {
                    switch(keyword) {
                        case Narrative:
                            if(narrative!=null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_multipleNarrative, "Only one 'Narrative:' element is allowed", IMarker.SEVERITY_ERROR);
							else
                                narrative = part;
                            break;
                        case InOrderTo:
                            if(narrative==null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element", IMarker.SEVERITY_ERROR);
							else if(inOrderTo!=null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_multipleInOrderTo, "Only one 'In order to' element is allowed", IMarker.SEVERITY_ERROR);
							else
                                inOrderTo = part;
                            break;
                        case AsA:
                            if(narrative==null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element", IMarker.SEVERITY_ERROR);
							else if(asA!=null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_multipleAsA, "Only one 'As a' element is allowed", IMarker.SEVERITY_ERROR);
							else
                                asA = part;
                            break;
                        case IWantTo:
                            if(narrative==null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element", IMarker.SEVERITY_ERROR);
							else if(asA==null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element", IMarker.SEVERITY_ERROR);
							else if(iWantTo!=null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_multipleIWantTo, "Only one 'I want to' element is allowed", IMarker.SEVERITY_ERROR);
							else
                                iWantTo = part;
                            break;
                        case SoThat:
                            if(narrative==null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_missingNarrative, "Missing 'Narrative:' element", IMarker.SEVERITY_ERROR);
							else if(asA==null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element", IMarker.SEVERITY_ERROR);
							else if(iWantTo==null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to' element", IMarker.SEVERITY_ERROR);
							else if(soThat!=null)
								part.addMark(Marks.Code.InvalidNarrativeSequence_multipleSoThat, "Only one 'So that' element is allowed", IMarker.SEVERITY_ERROR);
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
                      asA.addMark(Marks.Code.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to' element", IMarker.SEVERITY_ERROR);
                    }
                }
                else {
                    inOrderTo.addMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element", IMarker.SEVERITY_ERROR);
                }
            } else if (soThat!=null){
                if(asA!=null) {
                    if(iWantTo==null) {
                      asA.addMark(Marks.Code.InvalidNarrativeSequence_missingIWantTo, "Missing 'I want to' element", IMarker.SEVERITY_ERROR);
                    }
                }
                else {
                	soThat.addMark(Marks.Code.InvalidNarrativeSequence_missingAsA, "Missing 'As a' element", IMarker.SEVERITY_ERROR);
                }
            }
        }
        
    }

    protected void validateSteps(final fj.data.List<ValidatingPart> parts) throws JavaModelException {
        final fj.data.List<ValidatingPart> steps = parts.filter(new F<ValidatingPart,Boolean>() {
            public Boolean f(ValidatingPart part) {
                return Keyword.isStep(part.getKeyword());
            };
        });
        
        log.debug("Validating steps");
        
        StepLocator locator = project.getStepLocator();
        locator.traverseSteps(new Visitor<StepCandidate, Object>() {
            @Override
            public void visit(StepCandidate candidate) {
                log.debug("Evaluating step candidate: <{}>", candidate);
                for (ValidatingPart part : steps) {
                    part.evaluateCandidate(candidate);
                }
            }
        });
        
        log.debug("All step candidates have been evaluated");

        for (ValidatingPart step : steps) {
            String stepAsText = step.text();            		
            ConcurrentLinkedQueue<StepCandidate> candidates = step.getCandidates();
            int count = candidates.size();
            log.debug("#" + count + " candidate(s) found for >>" + Strings.escapeNL(stepAsText) + "<<");
            if (count == 0) {
				step.addMark(Marks.Code.NoMatchingStep, "No step is matching <" + stepAsText + ">", IMarker.SEVERITY_WARNING);
            } else if (count > 1){                
                fj.data.List<Integer> priorities = iterableList(candidates).map(new TransformByPriority());
                int max = priorities.maximum(Ord.intOrd);
                int countWithMax = priorities.filter(Equal.intEqual.eq(max)).length();
                if (countWithMax > 1){
                    MarkData mark = step.addMark(Marks.Code.MultipleMatchingSteps, "Ambiguous step: " + count + " steps are matching <" + stepAsText + "> got: " + candidates, IMarker.SEVERITY_WARNING);
                    Marks.putStepsAsHtml(mark, candidates);
                } else {
                    MarkData mark = step.addMark(Marks.Code.MultipleMatchingSteps_PrioritySelection, "Multiple steps matching, but only one with the highest priority for <" + stepAsText + ">", IMarker.SEVERITY_INFO);
                    Marks.putStepsAsHtml(mark, candidates);
                    log.debug("#{} matching steps but only one with the highest priority for {}", candidates.size(), stepAsText);
                }
            }
        }
    }
    
}
