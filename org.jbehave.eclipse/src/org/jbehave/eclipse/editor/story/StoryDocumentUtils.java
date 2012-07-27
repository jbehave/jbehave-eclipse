package org.jbehave.eclipse.editor.story;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.parser.VisitingStoryParser;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.parser.VisitingCollector;
import org.jbehave.eclipse.parser.StoryVisitor;
import org.jbehave.eclipse.util.Ref;

public class StoryDocumentUtils {
    
    private LocalizedStepSupport localizedStepSupport;

    public StoryDocumentUtils(LocalizedStepSupport localizedStepSupport) {
        this.localizedStepSupport = localizedStepSupport;
    }

    public List<StoryElement> getStoryElements(IDocument document) {
        VisitingCollector collector = new VisitingCollector();
        traverseStory(document, collector);
        return collector.getElements();
    }

    public void traverseStory(IDocument document, StoryVisitor visitor) {
        if(document instanceof StoryDocument) {
            ((StoryDocument)document).traverseStory(visitor);
        }
        else {
            new VisitingStoryParser(localizedStepSupport).parse(document.get(), visitor);
        }
    }

    public Ref<StoryElement> findStoryElementAtOffset(IDocument document, int offset) {
        if(offset>0){
            offset--; // one search from the character just behind the caret not after
        }
        return findStoryElementAtRegion(document, new Region(offset, 1));
    }
    
    public Ref<StoryElement> findStoryElementAtRegion(IDocument document, final IRegion region) {
        final Ref<StoryElement> ref = Ref.create();
        StoryVisitor visitor = new StoryVisitor() {
            @Override
            public void visit(StoryElement element) {
                if(element.intersects(region.getOffset(), region.getLength())) {
                    ref.set(element);
                    done();
                }
            }
        };
        traverseStory(document, visitor);
        return ref;
    }

}
