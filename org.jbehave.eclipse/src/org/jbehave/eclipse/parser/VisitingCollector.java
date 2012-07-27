package org.jbehave.eclipse.parser;

import java.util.List;

import org.jbehave.eclipse.util.New;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VisitingCollector extends StoryVisitor {
    
    private Logger logger = LoggerFactory.getLogger(VisitingCollector.class);
    
    private final List<StoryElement> elements = New.arrayList();

    @Override
    public void visit(StoryElement element) {
        logger.debug("Collecting element {}", element);
        elements.add(element);
    }
    
    public List<StoryElement> getElements() {
        return elements;
    }
    
}
