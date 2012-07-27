package org.jbehave.eclipse.editor.story.completion;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension4;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension5;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.WeightedStep;

public class StepTemplateProposal extends TemplateProposal implements 
        ICompletionProposalExtension4, ICompletionProposalExtension5,
        ICompletionProposalExtension6, StepCompletionProposalMixin.Trait {
    
    private final LocalizedStepSupport jbehaveProject;
    private final String complete;
    private final String label;
    private final WeightedStep weightedStep;

    public StepTemplateProposal(
            LocalizedStepSupport jbehaveProject, //
            Template template, TemplateContext context, IRegion region, 
            String complete, String label, WeightedStep weightedStep) {
        super(template, context, region, null, 0);
        this.jbehaveProject = jbehaveProject;
        this.complete = complete;
        this.label = label;
        this.weightedStep = weightedStep;
    }
    
    @Override
    public LocalizedStepSupport getLocalizedStepSupport() {
        return jbehaveProject;
    }
    
    @Override
    public boolean isAutoInsertable() {
        return false;
    }

    @Override
    public String getDisplayString() {
        // by default it is <name> - <description>
        return getStyledDisplayString().getString();
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
    public StyledString getStyledDisplayString() {
        return StepCompletionProposalMixin.createStyledString(this);
    }
    
    @Override
    public Image getImage() {
        return StepCompletionProposalMixin.getImage(this);
    }

    @Override
    public Object getAdditionalProposalInfo(IProgressMonitor monitor) {
        return StepCompletionProposalMixin.getAdditionalHTML(this);
    }
    
}
