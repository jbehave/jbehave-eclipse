package org.jbehave.eclipse.editor.story.scanner;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

public class AllInOnePartitionScanner implements org.eclipse.jface.text.rules.IPartitionTokenScanner {

    private IDocument document;
    private boolean consumed;

    public void setRange(IDocument document,
            int offset,
            int length) {
        setPartialRange(document, offset, length, null, -1);
    }
    
    public void setPartialRange(IDocument document,
            int offset,
            int length,
            String contentType,
            int partitionOffset) {
        this.document = document;
        this.consumed = false;
    }
    
    public int getTokenLength() {
        return document.getLength();
    }
    
    public int getTokenOffset() {
        return 0;
    }
    
    public IToken nextToken() {
        if(!consumed) {
            consumed = true;
            return new Token(IDocument.DEFAULT_CONTENT_TYPE);
        }
        return Token.EOF;
    }

}
