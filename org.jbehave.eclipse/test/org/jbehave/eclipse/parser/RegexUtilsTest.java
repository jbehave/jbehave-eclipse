package org.jbehave.eclipse.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.eclipse.parser.RegexUtils.containsExampleTable;
import static org.jbehave.eclipse.parser.RegexUtils.removeComment;
import static org.jbehave.eclipse.parser.RegexUtils.removeTrailingComment;

import java.util.List;

import org.jbehave.eclipse.parser.RegexUtils;
import org.jbehave.eclipse.parser.RegexUtils.TokenizerCallback;
import org.jbehave.eclipse.util.New;
import org.junit.Test;

public class RegexUtilsTest {
    
    private static String NL = "\n";
    
    @Test
    public void containsExampleTableWithNoTable() {
        String content = "Given an account named 'networkAgent' with the following properties";
        assertThat(containsExampleTable(content), is(false));
    }
    
    @Test
    public void containsExampleTableWithNoTableButComment() {
        String content = "Given an account named 'networkAgent' with the following properties" + NL +
                         "!-- Some comment" + NL;
        assertThat(containsExampleTable(content), is(false));
    }

    @Test
    public void containsExampleTableWithTable() {
        String content = "Given an account named 'networkAgent' with the following properties" + NL +
                         "|key|value|" + NL +
                         "|Login|networkAgentLogin|" + NL +
                         "|Password|networkAgentPassword|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }
    
    @Test
    public void containsExampleTableWithTableAndComment() {
        String content = "Given an account named 'networkAgent' with the following properties" + NL +
                         "|key|value|" + NL +
                         "!-- Some comment" + NL + 
                         "|Login|networkAgentLogin|" + NL +
                         "|Password|networkAgentPassword|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }
    
    @Test
    public void containsExampleTableWithTableEdgeCase1() {
        String content = "Given an account named 'networkAgent' with the following properties" + NL +
                         "|-|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }

    @Test
    public void containsExampleTableWithTableEdgeCase2() {
        String content = "Given an account named 'networkAgent' with the following properties" + NL +
                         "|-a-|" + NL;
        assertThat(containsExampleTable(content), is(true));
    }
    
    @Test
    public void splitLine() {
        final String content = "Given an account named 'networkAgent' with the following properties" + NL +
                "|key|value|" + NL +
                "!-- Some comment" + NL + 
                "|Login|networkAgentLogin|" + NL +
                "|Password|networkAgentPassword|" + NL;

        TokenCollector collector = new TokenCollector();
        RegexUtils.splitLine(content,collector);
        List<String> tokens = collector.getTokens();
        assertThat(tokens.size(), equalTo(10));
        assertThat(tokens.get(0), equalTo("Given an account named 'networkAgent' with the following properties"));
        assertThat(tokens.get(1), equalTo(NL));
        assertThat(tokens.get(2), equalTo("|key|value|"));
        assertThat(tokens.get(3), equalTo(NL));
        assertThat(tokens.get(4), equalTo("!-- Some comment"));
        assertThat(tokens.get(5), equalTo(NL));
        assertThat(tokens.get(6), equalTo("|Login|networkAgentLogin|"));
        assertThat(tokens.get(7), equalTo(NL));
        assertThat(tokens.get(8), equalTo("|Password|networkAgentPassword|"));
        assertThat(tokens.get(9), equalTo(NL));
    }
    
    @Test
    public void splitLine_startsWithNL() {
        final String content = NL + "Given an account named 'networkAgent' with the following properties" + NL +
                "|key|value|" + NL +
                "!-- Some comment" + NL + 
                "|Login|networkAgentLogin|" + NL +
                "|Password|networkAgentPassword|" + NL;

        TokenCollector collector = new TokenCollector();
        RegexUtils.splitLine(content,collector);
        List<String> tokens = collector.getTokens();
        assertThat(tokens.size(), equalTo(11));
        assertThat(tokens.get(0), equalTo(NL));
        assertThat(tokens.get(1), equalTo("Given an account named 'networkAgent' with the following properties"));
        assertThat(tokens.get(2), equalTo(NL));
        assertThat(tokens.get(3), equalTo("|key|value|"));
        assertThat(tokens.get(4), equalTo(NL));
        assertThat(tokens.get(5), equalTo("!-- Some comment"));
        assertThat(tokens.get(6), equalTo(NL));
        assertThat(tokens.get(7), equalTo("|Login|networkAgentLogin|"));
        assertThat(tokens.get(8), equalTo(NL));
        assertThat(tokens.get(9), equalTo("|Password|networkAgentPassword|"));
        assertThat(tokens.get(10), equalTo(NL));
    }
    
    @Test
    public void removeComment_noComment () {
        String input = "Given an account named 'networkAgent'";
        assertThat(removeComment(input), equalTo(input));
    }
    
    @Test
    public void removeComment_onlyOneComment () {
        assertThat(removeComment("!-- Some comment"), equalTo(""));
    }
    
    @Test
    public void removeComment_onlySeveralComments () {
        assertThat(removeComment(
                "!-- Some comment" + NL + 
                "!-- Some other comment" + NL), equalTo(""));
        assertThat(removeComment(
                "!-- Some comment" + NL + 
                "!-- Some other comment"), equalTo(""));
    }
    
    @Test
    public void removeComment_withNLAndEndingWithComment () {
        assertThat(removeComment(
                "Given an account named 'networkAgent' with the following properties" + NL + 
                NL + 
                "!-- Some other comment" + NL), 
                equalTo("Given an account named 'networkAgent' with the following properties" + NL));
    }
    
    @Test
    public void removeComment_ex1 () {
        final String actual = NL + "Given an account named 'networkAgent' with the following properties" + NL +
                        "|key|value|" + NL +
                        "!-- Some comment" + NL + 
                        "|Login|networkAgentLogin|" + NL +
                        "|Password|networkAgentPassword|" + NL;
        final String expected = NL + "Given an account named 'networkAgent' with the following properties" + NL +
                        "|key|value|" + NL +
                        "|Login|networkAgentLogin|" + NL +
                        "|Password|networkAgentPassword|" + NL;
        assertThat(removeComment(actual), equalTo(expected));
    }
    
    @Test
    public void removeTrailingComment_noComment () {
        final String actual = NL + "Given an account named 'networkAgent'" + NL;
        final String expected = NL + "Given an account named 'networkAgent'" + NL;
        assertThat(removeTrailingComment(actual), equalTo(expected));
    }
    
    @Test
    public void removeTrailingComment_ex1 () {
        final String actual = NL + "Given an account named 'networkAgent'" + NL +
                NL + 
                "!-- Some comment" + NL;
        final String expected = NL + "Given an account named 'networkAgent'" + NL;
        assertThat(removeTrailingComment(actual), equalTo(expected));
    }
    
    @Test
    public void tokenize_() {
        final String actual = NL + "Given an account named 'networkAgent' with the following properties" + NL +
                        "|key|value|" + NL +
                        "!-- Some comment" + NL + 
                        "|Login|networkAgentLogin|" + NL +
                        "|Password|networkAgentPassword|" + NL;

        TokenCollector collector = new TokenCollector();
        RegexUtils.tokenize(RegexUtils.COMMENT_PATTERN, actual, collector);
        List<String> tokens = collector.getTokens();
        assertThat(tokens.size(), equalTo(3));
        assertThat(tokens.get(0), equalTo(NL + "Given an account named 'networkAgent' with the following properties" + NL +
                "|key|value|" + NL));
        assertThat(tokens.get(1), equalTo("!-- Some comment" + NL));
        assertThat(tokens.get(2), equalTo("|Login|networkAgentLogin|" + NL +
                "|Password|networkAgentPassword|" + NL));
    }
    
    private static class TokenCollector implements TokenizerCallback {
        private List<String> tokens = New.arrayList();
        @Override
        public void token(int startOffset, int endOffset, String token, boolean isDelimiter) {
            tokens.add(token);
        }
        public List<String> getTokens() {
            return tokens;
        }
    }
}
