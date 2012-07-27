package org.jbehave.eclipse.editor.story.outline;

import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.ViewerCell;
import org.jbehave.eclipse.ImageIds;
import org.jbehave.eclipse.editor.story.StoryPartition;
import org.jbehave.eclipse.editor.text.TextProvider;

public class QuickOutlineStyledLabelProvider extends StyledCellLabelProvider implements TextProvider {
    
    private ImageRegistry imageRegistry;
    private boolean displayDecoration = false;
    
    /**
     * @param imageRegistry used to retrieve the label's image
     * @see ImageIds#STEP_GIVEN
     * @see ImageIds#STEP_WHEN
     * @see ImageIds#STEP_THEN
     */
    public QuickOutlineStyledLabelProvider(ImageRegistry imageRegistry) {
        this.imageRegistry = imageRegistry;
    }

    @Override
    public void update(ViewerCell cell) {
        Object element = cell.getElement();
        if (element instanceof List) {
            cell.setText("/");
            return;
        }
        OutlineModel model = (OutlineModel) element;
        defineText(model, cell);
        defineImage(model, cell);
        
        super.update(cell);
    }
    
    public void setDisplayDecoration(boolean displayDecoration) {
        this.displayDecoration = displayDecoration;
    }
    
    public boolean isDisplayDecoration() {
        return displayDecoration;
    }
    
    public String textOf(Object element) {
        return ((OutlineModel)element).getContent();
    }

    private void defineText(OutlineModel model, ViewerCell cell) {
        StyledString styledString = new StyledString(textOf(model));
        
        cell.setText(styledString.toString());
        cell.setStyleRanges(styledString.getStyleRanges());
    }

    private void defineImage(OutlineModel model, ViewerCell cell) {
        String key = null;
        
        StoryPartition partition = model.getPartition();
        switch(partition) {
            case Narrative:
                key = ImageIds.NARRATIVE;
                break;
            case ExampleTable:
                key = ImageIds.EXAMPLE_TABLE;
                break;
            case Scenario:
                key = ImageIds.SCENARIO;
                break;
            case Step:
                switch(model.getKeyword()) {
                    case Given:
                        key = ImageIds.STEP_GIVEN;
                        break;
                    case When:
                        key = ImageIds.STEP_WHEN;
                        break;
                    case Then:
                        key = ImageIds.STEP_THEN;
                        break;
				default:
					break;
                }
		default:
			break;
        }

        if (key != null) {
            cell.setImage(imageRegistry.get(key));
        }
    }
}
