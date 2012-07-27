package org.jbehave.eclipse;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

public class KeywordImages {
    
	private ImageRegistry imageRegistry;

    public KeywordImages(ImageRegistry imageRegistry) {
        this.imageRegistry = imageRegistry;
    }
    
    public Image getImageFor(Keyword keyword) {
        switch(keyword) {
            case Given:
                return imageRegistry.get(ImageIds.STEP_GIVEN);
            case When:
                return imageRegistry.get(ImageIds.STEP_WHEN);
            case Then:
                return imageRegistry.get(ImageIds.STEP_THEN);
            case And:
                return imageRegistry.get(ImageIds.STEP_AND); 
            case GivenStories:
            case Meta:
            case MetaProperty:
                return imageRegistry.get(ImageIds.META); 
            case AsA:
            case InOrderTo:
            case IWantTo:
            case Narrative:
                return imageRegistry.get(ImageIds.NARRATIVE);
            case ExamplesTable:
            case ExamplesTableHeaderSeparator:
            case ExamplesTableIgnorableSeparator:
            case ExamplesTableRow:
            case ExamplesTableValueSeparator:
                return imageRegistry.get(ImageIds.EXAMPLE_TABLE);
            case Scenario:
                return imageRegistry.get(ImageIds.SCENARIO);
            case Ignorable:
                return imageRegistry.get(ImageIds.IGNORABLE);
        }
        return null;
    }

}
