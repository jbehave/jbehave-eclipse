package org.jbehave.eclipse.editor.story.completion;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.swt.widgets.Shell;
import org.jbehave.eclipse.editor.story.StoryPartition;
import org.jbehave.eclipse.editor.story.TokenConstants;

public class StepContentAssistant extends ContentAssistant {
    
    /*
    private static class StringInformationPresenter implements IInformationPresenter {
        public String updatePresentation(Display display, String hoverInfo,
                TextPresentation presentation, int maxWidth, int maxHeight) {
            return hoverInfo;
        }
        
    }
    
    private static class DefaultInformationControlCreator extends AbstractReusableInformationControlCreator {
        public IInformationControl doCreateInformationControl(Shell shell) {
            DefaultInformationControl defaultInformationControl = new DefaultInformationControl(shell, new StringInformationPresenter()) {
                @Override
                protected void createContent(Composite parent) {
                    super.createContent(parent);
                    Control[] children = parent.getChildren();
                    for (Control control : children) {
                        if (control instanceof StyledText) {
                            StyledText styledText = (StyledText) control;
                            styledText.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
                        }
                    }
                }
            };
            return defaultInformationControl;
        }
    }
    */
    
    
    public StepContentAssistant() {
        IContentAssistProcessor stepProcessor = new StepContentAssistProcessor(); 
        setContentAssistProcessor(stepProcessor, StoryPartition.Step.name());
        setContentAssistProcessor(stepProcessor, (String)TokenConstants.IGNORED.getData());
        setContentAssistProcessor(stepProcessor, IDocument.DEFAULT_CONTENT_TYPE);
        enableAutoActivation(true);
        setAutoActivationDelay(500);
        setProposalPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_BELOW);
        enableColoredLabels(true);
        setInformationControlCreator(new IInformationControlCreator() {
            public IInformationControl createInformationControl(
                    final Shell parent) {
                return new DefaultInformationControl(parent, true) {
                    @Override
                    public void setInformation(String content) {
                        content = content.replaceAll("[\r\n]+", "<br>");
                        super.setInformation(content);
                    }
                };
            }
        });
    }
    
    /*
    new IInformationControlCreator() {
        @Override
        public IInformationControl createInformationControl(Shell shell) {
            BrowserInformationControl infoCtrl = new BrowserInformationControl(shell, 
                    PreferenceConstants.APPEARANCE_JAVADOC_FONT, false);
            return infoCtrl;
        }
    };
    */
}
