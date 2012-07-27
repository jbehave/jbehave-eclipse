package org.jbehave.eclipse.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.jbehave.eclipse.util.StringMatcher;
import org.junit.Before;
import org.junit.Test;

public class StringMatcherTest {
    
    private StringMatcher matcher;
    
    @Before
    public void setup () {
        matcher = new StringMatcher();
    }

    @Test
    public void no_includes_no_excludes () {
        assertThat(matcher.isAccepted(null), is(true));
        assertThat(matcher.isAccepted(""), is(true));
        assertThat(matcher.isAccepted("yeahhhh"), is(true));
    }
    
    @Test
    public void one_include_no_excludes () {
        matcher.addGlobIncludes("*hhhh");
        assertThat(matcher.isAccepted(null), is(false));
        assertThat(matcher.isAccepted(""),   is(false));
        assertThat(matcher.isAccepted("yeahhhh"), is(true));
        assertThat(matcher.isAccepted("yeahhh"), is(false));
    }
    
    @Test
    public void two_includes_no_excludes () {
        matcher.addGlobIncludes("*hhhh", "b*hhh");
        assertThat(matcher.isAccepted(null), is(false));
        assertThat(matcher.isAccepted(""),   is(false));
        assertThat(matcher.isAccepted("yeahhhh"), is(true));
        assertThat(matcher.isAccepted("yeahhh"), is(false));
        assertThat(matcher.isAccepted("beahhh"), is(true));
    }
    
    @Test
    public void no_include_one_exclude () {
        matcher.addGlobExcludes("y*hhhh");
        assertThat(matcher.isAccepted(null), is(true));
        assertThat(matcher.isAccepted(""),   is(true));
        assertThat(matcher.isAccepted("yeahhhh"), is(false));
        assertThat(matcher.isAccepted("heahhhh"), is(true));
    }
    
    @Test
    public void one_include_one_exclude () {
        matcher.addGlobIncludes("*hhhh");
        matcher.addGlobExcludes("y*hhhh");
        assertThat(matcher.isAccepted(null), is(false));
        assertThat(matcher.isAccepted(""),   is(false));
        assertThat(matcher.isAccepted("yeahhhh"), is(false));
        assertThat(matcher.isAccepted("heahhhh"), is(true));
        assertThat(matcher.isAccepted("heahhh"), is(false));
    }
    
    @Test
    public void no_include_two_excludes () {
        matcher.addGlobExcludes("y*hhhh", "?e*");
        assertThat(matcher.isAccepted(null), is(true));
        assertThat(matcher.isAccepted(""),   is(true));
        assertThat(matcher.isAccepted("yeahhhh"), is(false));
        assertThat(matcher.isAccepted("hea"), is(false));
        assertThat(matcher.isAccepted("hheahhh"), is(true));
        assertThat(matcher.isAccepted("hheahhh"), is(true));
    }

}
