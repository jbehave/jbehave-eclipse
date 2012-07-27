package org.jbehave.eclipse.editor.story.scanner;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.Token;
import org.jbehave.eclipse.editor.text.TextAttributeProvider;

public class SingleTokenScanner extends RuleBasedScanner {

    private TextAttributeProvider textAttributeProvider;
    private String attributeKey;

    public SingleTokenScanner(TextAttributeProvider textAttributeProvider, String attributeKey) {
        this.textAttributeProvider = textAttributeProvider;
        this.attributeKey = attributeKey;
        initialize();
        textAttributeProvider.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                initialize();
            }
        });
    }
    
    private void initialize() {
        TextAttribute textAttribute = textAttributeProvider.get(attributeKey);
        setDefaultReturnToken(new Token(textAttribute));
    }
}
