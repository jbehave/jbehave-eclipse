package org.jbehave.eclipse.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.eclipse.Keyword.Given;
import static org.jbehave.eclipse.Keyword.InOrderTo;
import static org.jbehave.eclipse.Keyword.Narrative;

import org.jbehave.core.i18n.LocalizedKeywords;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.util.CharTree;
import org.junit.Before;
import org.junit.Test;

public class CharTreeTest {
    
    private CharTree<Keyword> tree;

    @Before
    public void setUp () {
        LocalizedKeywords keywords = new LocalizedKeywords();
        tree = new CharTree<Keyword>('/', null);
        for(Keyword keyword : Keyword.values())
            tree.push(keyword.asString(keywords), keyword);
        
    }
        
    @Test
    public void canFindElement() {
        assertThat(tree.lookup("Given"), equalTo(Given));
        assertThat(tree.lookup("Narrative:"), equalTo(Narrative));
        assertThat(tree.lookup("Given a user named \"Bob\""), equalTo(Given));
        assertThat(tree.lookup("In order to be more communicative"), equalTo(InOrderTo));
    }
    
    @Test
    public void missingElementReturnsNull() {
        assertThat(tree.lookup("Gaven"), equalTo(null));
        assertThat(tree.lookup("\n"), equalTo(null));
    }

}
