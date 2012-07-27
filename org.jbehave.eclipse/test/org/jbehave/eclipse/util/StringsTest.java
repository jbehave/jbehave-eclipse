package org.jbehave.eclipse.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.eclipse.util.Strings.removeTrailingNewlines;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.jbehave.eclipse.util.Strings;
import org.junit.Test;

public class StringsTest {
	
    @Test
    public void canRemoveTrailingNewlines() {
        assertThat(removeTrailingNewlines("a"), equalTo("a"));
        assertThat(removeTrailingNewlines("a\r\n"), equalTo("a"));
        assertThat(removeTrailingNewlines("a\n"), equalTo("a"));
        assertThat(removeTrailingNewlines("a\n\n"), equalTo("a"));
        assertThat(removeTrailingNewlines("a\r"), equalTo("a"));
        assertThat(removeTrailingNewlines("a\r\r"), equalTo("a"));
        assertThat(removeTrailingNewlines("a\nb\n"), equalTo("a\nb"));
    }
    
    @Test
    public void canRemoveLeadingSpaces() {
        assertThat(Strings.removeLeadingSpaces("   a  "), equalTo("a  "));
        assertThat(Strings.removeLeadingSpaces("   abc"), equalTo("abc"));
        assertThat(Strings.removeLeadingSpaces("   a c"), equalTo("a c"));
        assertThat(Strings.removeLeadingSpaces("      "), equalTo(""));
        assertThat(Strings.removeLeadingSpaces(""), equalTo(""));
        assertThat(Strings.removeLeadingSpaces(null), equalTo(null));
        assertThat(Strings.removeLeadingSpaces("  \n"), equalTo("\n"));
        
    }

    @Test
    public void canSubstringUntilOffset() throws IOException {
        String text = IOUtils.toString(getClass().getResourceAsStream("/data/tfdm_update-1.story"));
        String line = Strings.substringUntilOffset(text, 25);
        assertThat(line, equalTo("Given an inactive direct "));
    }
    
}

