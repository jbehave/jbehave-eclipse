package org.jbehave.eclipse.editor.story.completion;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.WeightedStep;

public class StepCompletionProposal implements ICompletionProposal,
    ICompletionProposalExtension4,    
    ICompletionProposalExtension5, ICompletionProposalExtension6, StepCompletionProposalMixin.Trait {

    private final LocalizedStepSupport stepSupport;
    private final Region replacementRegion;
    private final String complete;
    private final String label;
    private final WeightedStep weightedStep;
    private StyledString styledString;
    private IContextInformation contextInformation;

    public StepCompletionProposal(LocalizedStepSupport stepSupport, Region replacementRegion, String complete, String label, WeightedStep weightedStep) {
        super();
        this.stepSupport = stepSupport;
        this.replacementRegion = replacementRegion;
        this.complete = complete;
        this.label = label;
        this.weightedStep = weightedStep;
    }
    
    @Override
    public LocalizedStepSupport getLocalizedStepSupport() {
        return stepSupport;
    }
    
    @Override
    public boolean isAutoInsertable() {
        return false;
    }
    
    @Override
    public WeightedStep getWeightedStep() {
        return weightedStep;
    }
    
    @Override
    public String getComplete() {
        return complete;
    }
    
    @Override
    public String getLabel() {
        return label;
    }
    
    @Override
    public void apply(IDocument document) {
        try {
            document.replace(replacementRegion.getOffset(), replacementRegion.getLength(), complete);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAdditionalProposalInfo() {
        return StepCompletionProposalMixin.getAdditionalHTML(this);
    }

    @Override
    public IContextInformation getContextInformation() {
        return contextInformation;
    }

    @Override
    public String getDisplayString() {
        return getStyledDisplayString().getString();
    }

    @Override
    public Image getImage() {
        return StepCompletionProposalMixin.getImage(this);
    }

    @Override
    public Point getSelection(IDocument document) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StyledString getStyledDisplayString() {
        if(styledString==null)
            styledString = StepCompletionProposalMixin.createStyledString(this);
        return styledString;
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return StepCompletionProposalMixin.getAdditionalHTML(this);
    }

}
