package org.jbehave.eclipse.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.parsers.RegexPrefixCapturingPatternParser;
import org.jbehave.core.parsers.StepMatcher;
import org.jbehave.core.parsers.StepPatternParser;
import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.util.New;
import org.junit.Before;
import org.junit.Test;

public class StepPatternParserTest {

    private StepPatternParser parser;

    @Before
    public void setUp() {
        parser = new RegexPrefixCapturingPatternParser();
    }

    @Test
    public void canMatchStep() {
        StepMatcher matcher = parser.parseStep(StepType.WHEN, "a user clicks on $buttonId button");
        assertThat(matcher.parameterNames(), equalTo(new String[] { "buttonId" }));

        String text = "a user clicks on enter'n go button";
        assertThat(matcher.matches(text), is(true));
    }

    @Test
    public void parseStepPattern() {
        parseStepPattern("a user clicks on $buttonId button");
        parseStepPattern("a user clicks on $buttonId");
    }
    
    private void parseStepPattern(String input) {
        String prefix = "$";
        Pattern pattern = Pattern.compile("(\\" + prefix + "\\w*)(\\W|\\Z)", Pattern.DOTALL);

        Matcher matcher = pattern.matcher(input);
        List<Token> frags = New.arrayList();

        int prev = 0;
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (start > 0) {
                frags.add(new Token(prev, start - prev, false, ""));
            }
            frags.add(new Token(start, end - start, true, matcher.group(2)));
            prev = end;
        }
        if (prev < input.length()) {
            frags.add(new Token(prev, input.length() - prev, false, ""));
        }

        for (Token token : frags) {
            System.out.println(">>" + token.value(input) + "<< isIdentifier: " + token);
        }
    }

    public class Token {
        public final int offset;
        public final int length;
        public final boolean isIdentifier;
        public final String whitespaceIfAny;

        public Token(int offset, int length, boolean isIdentifier, String whitespaceIfAny) {
            this.offset = offset;
            this.length = length;
            this.isIdentifier = isIdentifier;
            this.whitespaceIfAny = whitespaceIfAny;
        }

        public String value(String content) {
            return content.substring(offset, offset + length);
        }

        @Override
        public String toString() {
            return "Token [offset=" + offset + ", length=" + length + ", isIdentifier=" + isIdentifier + ", whitespaceIfAny='" + whitespaceIfAny + "']";
        }
    }
}
