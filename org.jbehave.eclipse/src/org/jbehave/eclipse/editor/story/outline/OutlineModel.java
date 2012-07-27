package org.jbehave.eclipse.editor.story.outline;

import java.util.ArrayList;
import java.util.List;

import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.editor.story.StoryPartition;

public class OutlineModel {

    private final StoryPartition partition;
    private Keyword keyword;
    private String content;
    private int offset, length;
    private List<OutlineModel> children = new ArrayList<OutlineModel>();
    
    public OutlineModel(Keyword keyword, String content, int offset, int length) {
        super();
        this.keyword = keyword;
        this.partition = StoryPartition.partitionOf(keyword);
        this.content = extractSingleLine(content);
        this.offset = offset;
        this.length = length;
    }
    
    private static String extractSingleLine(String content) {
        String[] lines = content.split("[\r\n]+");
        for(String line : lines) {
            if(!line.trim().isEmpty())
                return line;
        }
        return null;
    }
    
    public Keyword getKeyword() {
        return keyword;
    }

    public boolean hasChildren() {
        return !children.isEmpty();
    }
    
    public List<OutlineModel> getChildren() {
        return children;
    }
    
    boolean merge(OutlineModel model) {
        if(partition==model.partition) {
            // make sure there is no hole: otherwise the model must be emitted
            if(this.offset+this.length == model.offset) {
                appendChild(model);
                this.length += model.length;
                return true;
            }
        }
        return false;
    }

    private void appendChild(OutlineModel model) {
        if(children.isEmpty()) {
            // add self as child
            // children.add(copy());
        }
        children.add(model);
    }

    public StoryPartition getPartition() {
        return partition;
    }
    
    public String getContent() {
        return content;
    }
    
    public int getOffset() {
        return offset;
    }

    public int getLength() {
        return length;
    }
    
}
