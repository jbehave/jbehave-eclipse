package org.jbehave.eclipse.editor.story.outline;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.parser.StoryVisitor;
import org.jbehave.eclipse.util.New;

public class OutlineModelBuilder extends StoryVisitor {
    
    private final LocalizedStepSupport jbehaveProject;
    private final IDocument document;
    private List<OutlineModel> models;
    
    public OutlineModelBuilder(LocalizedStepSupport jbehaveProject, IDocument document) {
        this.jbehaveProject = jbehaveProject;
        this.document = document;
    }
    
    public List<OutlineModel> build () {
        models = New.arrayList();
        new StoryDocumentUtils(jbehaveProject).traverseStory(document, this);
        return models;
    }
    
    @Override
    public final void done() {
        // prevent any state change, since one can reuse the visitor behavior
    }

    @Override
    public void visit(StoryElement element) {
        OutlineModel model = new OutlineModel(
                element.getPreferredKeyword(), 
                element.getContent(), 
                element.getOffset(), 
                element.getLength());
        
        if(!acceptModel(model)) {
            return;
        }
        
        if(models.isEmpty()) {
            models.add(model);
            return;
        }
        
        // pick last, merge it or add it to the list
        OutlineModel last = models.get(models.size()-1);
        if(!last.merge(model))
            models.add(model);
    }

    protected boolean acceptModel(OutlineModel model) {
        switch(model.getPartition()) {
            case Narrative:
            case Scenario:
            case ExampleTable:
            case Step:
                return true;
		default:
			break;
        }
        return false;
    }
    
}
