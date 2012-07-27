package org.jbehave.eclipse.editor.story.scanner;

import java.util.List;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.story.StoryDocumentUtils;
import org.jbehave.eclipse.editor.story.StoryPartition;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.parser.StoryVisitor;
import org.jbehave.eclipse.util.New;

public class StoryPartitionScanner implements org.eclipse.jface.text.rules.IPartitionTokenScanner {

    private final LocalizedStepSupport localizedStepSupport;
    //
    private IDocument document;
    //
    private int cursor;
    private Partition currentPartition;
    private List<Partition> partitions;
    
    public StoryPartitionScanner(JBehaveProject jbehaveProject) {
        this.localizedStepSupport = jbehaveProject.getLocalizedStepSupport();
    }
    
    @Override
    public void setRange(IDocument document,
            int offset,
            int length) {
        setPartialRange(document, offset, length, null, -1);
    }
    
    @Override
    public void setPartialRange(IDocument document,
            int offset,
            int length,
            String contentType,
            int partitionOffset) {
        this.document = document;
        initializePartitions();
    }
    
    @Override
    public int getTokenLength() {
        return currentPartition.length;
    }
    
    @Override
    public int getTokenOffset() {
        return currentPartition.offset;
    }
    
    @Override
    public IToken nextToken() {
        if(cursor<partitions.size()) {
            currentPartition = partitions.get(cursor++);
            return new Token(currentPartition.keyword.name());
        }
        return Token.EOF;
    }

    private void initializePartitions() {
        partitions = New.arrayList();
        cursor = 0;
        
        StoryVisitor visitor = new StoryVisitor() {
            @Override
            public void visit(StoryElement part) {
                push(part);
            }
        };
        new StoryDocumentUtils(localizedStepSupport).traverseStory(document, visitor);
    }
    
    private void push(StoryElement part) {
        StoryPartition partition = StoryPartition.partitionOf(part.getPreferredKeyword());
        Partition p = new Partition(
                partition,
                part.getOffset(),
                part.getLength());
        
        if(partitions.isEmpty()) {
            partitions.add(p);
            return;
        }
        
        // pick last, merge it or add it to the list
        Partition last = partitions.get(partitions.size()-1);
        if(!last.merge(p))
            partitions.add(p);
    }
    
    private class Partition {
        private StoryPartition keyword;
        private int offset;
        private int length;
        public Partition(StoryPartition keyword, int offset, int length) {
            this.keyword = keyword;
            this.offset = offset;
            this.length = length;
        }
        public boolean merge(Partition p) {
            if(keyword==p.keyword) {
                this.length += p.length;
                return true;
            }
            return false;
        }
        @Override
        public String toString() {
            return "P["+keyword+", offset: " + offset + ", length: " + length + "]";
        }
    }
    
}
