package org.jbehave.eclipse.util;

import java.util.List;
import java.util.regex.Pattern;

import fj.F;
import fj.function.Booleans;

public class StringMatcher {
    
    private final List<Pattern> includePatterns = New.arrayList();
    private final List<Pattern> excludePatterns = New.arrayList();
    private F<String,Boolean> cached;
    
    public StringMatcher() {
    }
    
    public StringMatcher addRegexIncludes(String...regexes) {
        compileRegexTo(includePatterns, regexes);
        invalidateCached();
        return this;
    }
    
    public StringMatcher addRegexExcludes(String...regexes) {
        compileRegexTo(excludePatterns, regexes);
        invalidateCached();
        return this;
    }
    
    private static void compileRegexTo(List<Pattern> patterns, String... regexes) {
        for(int i=0;i<regexes.length;i++) {
            patterns.add(Pattern.compile(regexes[i]));
        }
    }
    
    public StringMatcher addGlobIncludes(String...globs) {
        compileGlobTo(includePatterns, globs);
        invalidateCached();
        return this;
    }
    
    public StringMatcher addGlobExcludes(String...globs) {
        compileGlobTo(excludePatterns, globs);
        invalidateCached();
        return this;
    }
    
    private void invalidateCached() {
        cached = null;
    }

    private static void compileGlobTo(List<Pattern> patterns, String... globs) {
        for(int i=0;i<globs.length;i++) {
            patterns.add(Strings.convertGlobToPattern(globs[i]));
        }
    }
    
    public F<String,Boolean> compile () {
        if(cached==null) {
            F<String,Boolean> includesF = compileIncludes(includePatterns);
            F<String,Boolean> excludesF = compileExcludes(excludePatterns);
            cached = FJ.and(includesF, excludesF);
        }
        return cached;
    }
    
    public boolean isAccepted(String input) {
        return compile().f(input);
    }

    public static F<String, Boolean> compileIncludes(List<Pattern> includePatterns) {
        if(includePatterns.isEmpty())
            return FJ.alwaysTrue();
        
        final Pattern[] patterns = includePatterns.toArray(new Pattern[includePatterns.size()]);
        return new F<String, Boolean> () {
            @Override
            public Boolean f(String value) {
                // non empty case: null is rejected
                if(value == null)
                    return false;
                
                for(Pattern pattern : patterns) {
                    if(pattern.matcher(value).matches()) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    public static F<String, Boolean> compileExcludes(List<Pattern> excludePatterns) {
        if(excludePatterns.isEmpty())
            return FJ.alwaysTrue();
        return Booleans.not(compileIncludes(excludePatterns));
    }
}
