package org.jbehave.eclipse.editor.story;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * Detects "word" corresponding to step parameters $xxxx
 */
class ParameterWordDetector implements IWordDetector {
    /**
     * @see org.eclipse.jface.text.rules.IWordDetector#isWordStart(char)
     */
    public boolean isWordStart(char c) {
        return c == '$';
    }
    
    /**
     * @see org.eclipse.jface.text.rules.IWordDetector#isWordPart(char)
     */
    public boolean isWordPart(char c) {
        return Character.isJavaIdentifierPart(c);
    }
}