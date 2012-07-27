package org.jbehave.eclipse.parser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.eclipse.util.New;

public class RegexUtils {
    
    public static Pattern EXAMPLE_TABLE_PATTERN = Pattern.compile("^\\s*\\|([^-]|\\-[^-]*)", Pattern.MULTILINE);
    public static boolean containsExampleTable(String content) {
        return EXAMPLE_TABLE_PATTERN.matcher(content).find();
    }
    
    public interface TokenizerCallback {
        void token(int startOffset, int endOffset, String token, boolean isDelimiter);
    }
    
    public static Pattern LINE_PATTERN = Pattern.compile("[\r\n]+");
    public static void splitLine(String input, TokenizerCallback callback) {
        tokenize(LINE_PATTERN, input, callback);
    }

    public static void tokenize(Pattern pattern, String input, TokenizerCallback callback) {
        Matcher matcher = pattern.matcher(input);
        int index = 0;
        while(matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if(start>index) {
                callback.token(index, start, input.substring(index, start), false);
            }
            callback.token(start, end, input.substring(start, end), true);
            index = end;
        }
        if(index<input.length()){
            callback.token(index, input.length(), input.substring(index), false);
        }
    }
    
    public static Pattern COMMENT_PATTERN = Pattern.compile("^\\s*!--[^\r\n]*[\r\n]{0,2}", Pattern.MULTILINE);     
    public static String removeComment(String input) {
        return COMMENT_PATTERN.matcher(input).replaceAll("");
    }
    
    public static String removeTrailingComment(String input) {
        class Token {
            String content;
            boolean isDelimiter;
            public Token(String content, boolean isDelimiter) {
                this.content = content;
                this.isDelimiter = isDelimiter;
            }
        }
        final List<Token> tokens = New.arrayList();
        tokenize(COMMENT_PATTERN, input, new TokenizerCallback() {
            @Override
            public void token(int startOffset, int endOffset, String token, boolean isDelimiter) {
                tokens.add(new Token(token, isDelimiter));
            }
        });
        int lastIndex = tokens.size()-1;
        for(;lastIndex>=0;lastIndex--) {
            if(!tokens.get(lastIndex).isDelimiter){
                break;
            }
        }
        
        if(lastIndex == tokens.size()-1){
            // nothing to remove, return as is
            return input;
        }
        StringBuilder builder = new StringBuilder ();
        for(int i=0;i<=lastIndex;i++){
            builder.append(tokens.get(i).content);
        }
        return builder.toString();
    }
    

}
