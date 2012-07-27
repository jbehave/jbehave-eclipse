package org.jbehave.eclipse.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jbehave.eclipse.util.StringDecorator.decorate;

import org.junit.Test;

public class StringDecoratorTest {

    @Test
    public void isTheStartOfOneOf () {
        assertThat(decorate("giv").isStartOfOneOfIgnoringCase("and", "when", "given", "then"), is(true));
        assertThat(decorate("gIv").isStartOfOneOfIgnoringCase("and", "when", "given", "then"), is(true));
        assertThat(decorate("gav").isStartOfOneOfIgnoringCase("and", "when", "given", "then"), is(false));
        assertThat(decorate("givening").isStartOfOneOfIgnoringCase("and", "when", "given", "then"), is(false));
    }
}
