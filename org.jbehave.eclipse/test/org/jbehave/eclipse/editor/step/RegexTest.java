package org.jbehave.eclipse.editor.step;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.eclipse.Keyword;
import org.junit.Test;

public class RegexTest {

    private static Pattern parameterPattern = Pattern.compile("\\$[a-zA-Z0-9\\-_]+");
    
    @Test
    public void usecaseEx1() {
        String text = "When a user clicks on $buttonId button";
        
        Matcher matcher = parameterPattern.matcher(text);
        int prev = 0;
        assertThat(matcher.find(), is(true));
        int start = matcher.start();
        int end   = matcher.end();
        assertThat(text.substring(prev, start), equalTo("When a user clicks on "));
        assertThat(text.substring(start, end), equalTo("$buttonId"));
        prev = end;
        assertThat(matcher.find(), is(false));
        assertThat(text.substring(prev), equalTo(" button"));
    }
    
    @Test
    public void usecaseEx2 () {
        String content = "As a developer\nI want to develop efficiently";
        for(Keyword keyword : Keyword.values()) {
            String asString = keyword.asString();
            if(asString.endsWith(":"))
                asString = asString.substring(0,asString.length()-1);
            String regex = "^("+Pattern.quote(asString)+")";
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE|Pattern.CASE_INSENSITIVE);
            content = pattern.matcher(content).replaceAll("<b>$1</b>");
        }
        assertThat(content, equalTo("<b>As a</b> developer\n<b>I want to</b> develop efficiently"));
    }
}
