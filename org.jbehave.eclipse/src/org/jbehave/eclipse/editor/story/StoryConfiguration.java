package org.jbehave.eclipse.editor.story;

import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.story.completion.StepContentAssistant;
import org.jbehave.eclipse.editor.story.scanner.AllInOneScanner;
import org.jbehave.eclipse.editor.story.scanner.ExampleTableScanner;
import org.jbehave.eclipse.editor.story.scanner.MetaScanner;
import org.jbehave.eclipse.editor.story.scanner.NarrativeScanner;
import org.jbehave.eclipse.editor.story.scanner.ScenarioScanner;
import org.jbehave.eclipse.editor.story.scanner.SingleTokenScanner;
import org.jbehave.eclipse.editor.story.scanner.StepScanner;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;
import org.jbehave.eclipse.editor.text.style.TextStyle;

public class StoryConfiguration extends SourceViewerConfiguration {

    private ITokenScanner stepScanner;
    private ITokenScanner defaultScanner;
    private ITokenScanner commentScanner;
    private ITokenScanner scenarioScanner;
    private ITokenScanner narrativeScanner;
    private ITokenScanner miscScanner;
    private ITokenScanner exampleTableScanner;
    private TextAttributeProvider textAttributeProvider;
    private StoryEditor storyEditor;
    private PresentationReconciler reconciler;
    private Object reconcilerInternalListener;

    public StoryConfiguration(StoryEditor storyEditor, TextAttributeProvider textAttributeProvider) {
        this.storyEditor = storyEditor;
        this.textAttributeProvider = textAttributeProvider;
    }
    
    public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
        List<String> types = StoryPartition.names();
        types.add(IDocument.DEFAULT_CONTENT_TYPE);
        return types.toArray(new String[types.size()]);
    }
    
    @Override
    public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
        return new StepContentAssistant();
    }

    public IHyperlinkDetector[] getHyperlinkDetectors(final ISourceViewer sourceViewer) {
        return new IHyperlinkDetector[] { new StepHyperLinkDetector() };
    }

    private ITokenScanner getDefaultScanner() {
        if (defaultScanner == null) {
            defaultScanner = createScanner(TextStyle.DEFAULT);
        }
        return defaultScanner;
    }

    protected ITokenScanner getStepScanner() {
        if (stepScanner == null) {
            stepScanner = new StepScanner(getJBehaveProject(), textAttributeProvider);
        }
        return stepScanner;
    }

    protected JBehaveProject getJBehaveProject() {
        return storyEditor.getJBehaveProject();
    }
    
    protected ITokenScanner getCommentScanner() {
        if (commentScanner == null) {
            commentScanner = createScanner(TextStyle.COMMENT);
        }
        return commentScanner;
    }
    
    protected ITokenScanner getScenarioScanner() {
        if (scenarioScanner == null) {
            scenarioScanner = new ScenarioScanner(getJBehaveProject(), textAttributeProvider);
        }
        return scenarioScanner;
    }
    
    protected ITokenScanner getNarrativeScanner() {
        if (narrativeScanner == null) {
            narrativeScanner = new NarrativeScanner(getJBehaveProject(), textAttributeProvider);
        }
        return narrativeScanner;
    }
    
    protected ITokenScanner getMiscScanner() {
        if (miscScanner == null) {
            miscScanner = new MetaScanner(getJBehaveProject(), textAttributeProvider);
        }
        return miscScanner;
    }
    
    protected ITokenScanner getExampleTableScanner() {
        if (exampleTableScanner == null) {
            exampleTableScanner = new ExampleTableScanner(getJBehaveProject(), textAttributeProvider);
        }
        return exampleTableScanner;
    }

    private ITokenScanner createScanner(String attributeKey) {
        return new SingleTokenScanner(textAttributeProvider, attributeKey);
    }

    @Override
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
        reconciler = new PresentationReconciler() {
        };
        
        try {
            Field declaredField = PresentationReconciler.class.getDeclaredField("fInternalListener");
            declaredField.setAccessible(true);
            reconcilerInternalListener = declaredField.get(reconciler);
        } catch (Exception e) {
            Activator.logError("Failed to retrieve internal listener", e);
        }

        DefaultDamagerRepairer dr;
        if(AllInOneScanner.allInOne) {
            dr = new DefaultDamagerRepairer(new AllInOneScanner(getJBehaveProject(), textAttributeProvider));
            reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
            reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        }
        else {
            dr = new DefaultDamagerRepairer(getDefaultScanner());
            reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
            reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
    
            dr = new DefaultDamagerRepairer(getStepScanner());
            reconciler.setDamager(dr, StoryPartition.Step.name());
            reconciler.setRepairer(dr, StoryPartition.Step.name());
    
            dr = new DefaultDamagerRepairer(getCommentScanner());
            reconciler.setDamager(dr, StoryPartition.Comment.name());
            reconciler.setRepairer(dr, StoryPartition.Comment.name());
    
            dr = new DefaultDamagerRepairer(getScenarioScanner());
            reconciler.setDamager(dr, StoryPartition.Scenario.name());
            reconciler.setRepairer(dr, StoryPartition.Scenario.name());
            
            dr = new DefaultDamagerRepairer(getNarrativeScanner());
            reconciler.setDamager(dr, StoryPartition.Narrative.name());
            reconciler.setRepairer(dr, StoryPartition.Narrative.name());
            
            dr = new DefaultDamagerRepairer(getExampleTableScanner());
            reconciler.setDamager(dr, StoryPartition.ExampleTable.name());
            reconciler.setRepairer(dr, StoryPartition.ExampleTable.name());
            
            dr = new DefaultDamagerRepairer(getMiscScanner());
            reconciler.setDamager(dr, StoryPartition.Meta.name());
            reconciler.setRepairer(dr, StoryPartition.Meta.name());
        }
        
        return reconciler;
    }
    
    @Override
    public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
        return new StoryAnnotationHover(storyEditor);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getReconcilerInternalListener(Class<T> asType) {
        return (T)reconcilerInternalListener;
    }
}