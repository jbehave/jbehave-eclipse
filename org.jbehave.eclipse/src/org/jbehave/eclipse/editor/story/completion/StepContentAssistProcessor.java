package org.jbehave.eclipse.editor.story.completion;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.JBehaveProjectRegistry;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.EditorUtils;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.StepSupport;
import org.jbehave.eclipse.editor.step.WeightedStep;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.editor.text.TemplateUtils;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.util.Lists;
import org.jbehave.eclipse.util.New;
import org.jbehave.eclipse.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepContentAssistProcessor implements IContentAssistProcessor {
    
    private Logger logger = LoggerFactory.getLogger(StepContentAssistProcessor.class);
    
    @Override
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, final int offset) {
        try {
            IProject project = EditorUtils.findProject(viewer);
            JBehaveProject jbehaveProject = JBehaveProjectRegistry.get().getOrCreateProject(project);
            LocalizedStepSupport localizedStepSupport = jbehaveProject.getLocalizedStepSupport();

            IDocument document = viewer.getDocument();
            int lineNo = document.getLineOfOffset(offset);
            int lineOffset = document.getLineOffset(lineNo);
            boolean isWithinLine = (lineOffset < offset);
            
            String partitionText = "";
            int index = offset;
            String lineStart = "";

            boolean relyOnPartition = false;
            if(offset>0) {
                // retrieve region before 'cause we are probably in the next one
                ITypedRegion region = document.getPartition(offset-1);
                int partitionOffset = region.getOffset();
                int partitionLength = region.getLength();
                index = (offset - 1) - partitionOffset;
                partitionText = document.get(partitionOffset, partitionLength);
                if(isWithinLine) {
                    lineStart = Strings.substringUntilOffset(partitionText, index+1);
                }
                
                // keep partition infos for logging, but search line content by an other way
                if(!relyOnPartition) {
                    logger.debug("Autocompletion retrieving content lineOffset: " + lineOffset + ", offset: " + offset);
                    lineStart = document.get(lineOffset, offset-lineOffset);
                }
            }
            
            logger.debug("Autocompletion offset: {} partition text: {}", offset, partitionText);
            logger.debug("Autocompletion line start: {}", lineStart);

            if(StringUtils.isEmpty(lineStart)) {
                return createKeywordCompletionProposals(jbehaveProject, offset, 0, viewer);
            }
            else if(StepSupport.isStartOfStepIgnoringCase(localizedStepSupport, lineStart) //
                    && !StepSupport.isStep(localizedStepSupport, lineStart)) {
                return createKeywordCompletionProposals(jbehaveProject, lineOffset, lineStart.length(), viewer);
            }
            
            // TODO add support for multi-line step
            final String stepStart = lineStart;
            
            String stepStartUsedForSearch = stepStart;
            // special case: one must find the right type of step
            boolean isAndStep = StepSupport.isStepAnd(localizedStepSupport, lineStart); 
            if(isAndStep) {
                StoryElement element = new StoryDocumentUtils(localizedStepSupport).findStoryElementAtOffset(document, offset).get();
                Keyword keyword = element.getPreferredKeyword();
                if(keyword == Keyword.And) {
                    logger.debug("Autocompletion unable to disambiguate 'And' case: previous story element is probably not a step");
                    return null;
                }
                int indexOf = localizedStepSupport.and(false).length();
                stepStartUsedForSearch = keyword.asString(localizedStepSupport.getLocalizedKeywords()) + lineStart.substring(indexOf);
            }
            
            logger.debug("Autocompletion step start used for search: {}", stepStartUsedForSearch);
            
            List<WeightedStep> candidates = Lists.toList(jbehaveProject.getStepLocator().findCandidatesStartingWith(stepStartUsedForSearch));
            Collections.sort(candidates);
            logger.debug("Autocompletion found #{}", candidates.size());
            
            String stepWithKeyword = StepSupport.stepWithoutKeyword(localizedStepSupport, stepStart);
            Region regionFullLine = new Region(lineOffset, lineStart.length());
            Region regionComplete = new Region(offset, 0);
            
            TemplateContext contextFullLine = createTemplateContext(jbehaveProject, document, regionFullLine);
            TemplateContext contextComplete = createTemplateContext(jbehaveProject, document, regionComplete);

            List<ICompletionProposal> proposals = New.arrayList();
            for(int i=0;i<candidates.size();i++) {
                WeightedStep weightedStep = candidates.get(i);
                
                String displayString;
                String complete;
                TemplateContext templateContext;
                Region replacementRegion;
                if(!StringUtils.isBlank(stepWithKeyword)) {
                    complete = weightedStep.stepCandidate.getParametrizedStep().complete(stepWithKeyword);
                    templateContext = contextComplete;
                    replacementRegion = regionComplete;
                    displayString = lineStart + complete;
                }
                else {
                    complete = weightedStep.stepCandidate.fullStep();
                    if(isAndStep) {
                        complete = localizedStepSupport.and(false) + " " + weightedStep.stepCandidate.stepPattern;
                    }
                    templateContext = contextFullLine;
                    replacementRegion = regionComplete;
                    displayString = complete;
                }
                complete += "\n";

                int cursor = complete.indexOf('$');
                if(cursor<0) {
                    cursor = complete.length();
                    ICompletionProposal proposal = null;
                    int mode = 2;
                    switch(mode) {
                        case 1: 
                            proposal = new CompletionProposal(
                                complete,
                                replacementRegion.getOffset(),
                                replacementRegion.getLength(),
                                cursor,
                                null,
                                displayString,
                                null,
                                displayString);
                            break;
                        default:
                            proposal = new StepCompletionProposal(localizedStepSupport, replacementRegion, complete, displayString, weightedStep);
                    }
                    proposals.add(proposal);
                }
                else {
                    String templateText = TemplateUtils.templatizeVariables(complete);
                    Template template = new Template(
                            lineStart, 
                            displayString, 
                            StoryContextType.STORY_CONTEXT_TYPE_ID, templateText, false);
                    proposals.add(new StepTemplateProposal(localizedStepSupport,
                            template,
                            templateContext, replacementRegion, complete, displayString, weightedStep));
                }
            }
            
            return proposals.toArray(new ICompletionProposal[proposals.size()]);

        } catch (BadLocationException e) {
            e.printStackTrace();
        } 
        return null;
    }

    private DocumentTemplateContext createTemplateContext(JBehaveProject jbehaveProject, IDocument document, Region region) {
        TemplateContextType contextType = StoryContextType.getTemplateContextType();
        return new DocumentTemplateContext(contextType, document, region.getOffset(), region.getLength());
    }

    private ICompletionProposal[] createKeywordCompletionProposals(JBehaveProject jbehaveProject, int offset, int length, ITextViewer viewer) {
        List<ICompletionProposal> proposals = New.arrayList();
        
        LocalizedStepSupport localizedStepSupport = jbehaveProject.getLocalizedStepSupport();
        LocalizedKeywords localizedKeywords = localizedStepSupport.getLocalizedKeywords();

        Keyword[] keywords = new Keyword[] {
                Keyword.Given,
                Keyword.And,
                Keyword.When,
                Keyword.Then,
                Keyword.Ignorable,
                Keyword.Scenario,
                Keyword.GivenStories,
                Keyword.Narrative,
                Keyword.InOrderTo,
                Keyword.AsA,
                Keyword.IWantTo,
                Keyword.ExamplesTable
                };
        for(Keyword keyword : keywords) {
            String kw = keyword.asString(localizedKeywords);
            proposals.add(new CompletionProposal(kw, offset, length, kw.length()));
        };
        
        StoryTemplateCompletionProcessor t = new StoryTemplateCompletionProcessor(jbehaveProject);
        proposals.addAll(Arrays.asList(t.computeCompletionProposals(viewer, offset)));
        return proposals.toArray(new ICompletionProposal[proposals.size()]);
    }

    @Override
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    @Override
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    @Override
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    @Override
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }

    @Override
    public String getErrorMessage() {
        return "Could not assist with step content";
    }
}
