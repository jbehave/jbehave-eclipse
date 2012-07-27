package org.jbehave.eclipse;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.eclipse.util.CharTree;

public class JBehaveContext {
    
    private static JBehaveContext context;
    public static JBehaveContext get() {
        if(context==null)
            context = new JBehaveContext();
        return context;
    }
    
    private CharTree<Keyword> keywordTree;
    private Keywords keywords;
    
    public Keywords getKeywords() {
        if(keywords==null)
            keywords = new LocalizedKeywords();
        return keywords;
    }
    
    public CharTree<Keyword> getKeywordTree() {
        if(keywordTree==null) {
            Keywords kws = getKeywords();
            keywordTree = new CharTree<Keyword>('/', null);
            for(Keyword kw : Keyword.values()) {
                keywordTree.push(kw.asString(kws), kw);
            }
        }
        return keywordTree;
    }
    
}
