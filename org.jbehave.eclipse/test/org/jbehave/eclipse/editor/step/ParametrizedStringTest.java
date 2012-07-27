package org.jbehave.eclipse.editor.step;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.jbehave.eclipse.editor.step.ParametrizedStep;
import org.jbehave.eclipse.editor.step.ParametrizedStep.Token;
import org.jbehave.eclipse.editor.step.ParametrizedStep.WeightChain;
import org.junit.Test;

public class ParametrizedStringTest {

    @Test
    public void getParameters () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.getParameters(), equalTo(asList("name")));
    }
    
    @Test
    public void getParameters_parameterAtEnd () {
        ParametrizedStep s = new ParametrizedStep("a user named $name");
        assertThat(s.getParameters(), equalTo(asList("name")));
    }
    
    @Test
    public void getTokens () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        List<Token> tokens = s.getTokens();
        assertThat(tokens.get(0).value(), equalTo("a user named "));
        assertThat(tokens.get(1).value(), equalTo("name"));
        assertThat(tokens.get(2).value(), equalTo(" clicks"));
    }
    
    @Test
    public void getTokens_ex1() {
        ParametrizedStep s = new ParametrizedStep("a user named \"$name\"");
        List<Token> tokens = s.getTokens();
        assertThat(tokens.get(0).value(), equalTo("a user named \""));
        assertThat(tokens.get(1).value(), equalTo("name"));
        assertThat(tokens.get(2).value(), equalTo("\""));
    }

    @Test
    public void acceptsInputPart_noParameter () {
        assertThat(pString("a user clicks").acceptsBeginning("a use"), is(1));
        assertThat(pString("a user clicks").acceptsBeginning("a user clicks"), is(2));
        assertThat(pString("a user clicks").acceptsBeginning("a man"), is(0));
    }
    
    @Test
    public void weightChain_noParameter_ex1 () {
        ParametrizedStep s = new ParametrizedStep("a user clicks");
        
        WeightChain chain = s.calculateWeightChain("a use");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a use")));
    }
    
    @Test
    public void weightChain_noParameter_ex2 () {
        ParametrizedStep s = new ParametrizedStep("a user clicks");
        
        WeightChain chain = s.calculateWeightChain("a user clicks");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user clicks")));
    }
    
    @Test
    public void weightChain_noParameter_ex3 () {
        ParametrizedStep s = new ParametrizedStep("a user clicks");
        
        WeightChain chain = s.calculateWeightChain("a man");
        assertThat(chain.tokenize(), equalTo(Arrays.<String>asList()));
    }
    
    @Test
    public void complete_noParameter_ex1 () {
        ParametrizedStep s = new ParametrizedStep("a user clicks");
        
        String complete = s.complete("a use");
        assertThat(complete, equalTo("r clicks"));
    }
    
    @Test
    public void complete_noParameter_ex2 () {
        ParametrizedStep s = new ParametrizedStep("a user clicks");
        
        String complete = s.complete("a user clicks");
        assertThat(complete, equalTo(""));
    }
    
    @Test
    public void complete_noParameter_ex3 () {
        ParametrizedStep s = new ParametrizedStep("a user clicks");
        
        String complete = s.complete("a man");
        assertThat(complete, equalTo(""));
    }

    private static ParametrizedStep pString(String input) {
        return new ParametrizedStep(input);
    }

    @Test
    public void acceptsInputPart_withParameter_beforeParameter_insideLiteral_ex1 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.acceptsBeginning("a use"), is(1));
    }
    
    @Test
    public void weightChain_withParameter_beforeParameter_insideLiteral_ex1 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        WeightChain chain = s.calculateWeightChain("a use");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a use")));
    }
    
    @Test
    public void complete_withParameter_beforeParameter_insideLiteral_ex1 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        String complete = s.complete("a use");
        assertThat(complete, equalTo("r named $name clicks"));
    }

    @Test
    public void acceptsInputPart_withParameter_beforeParameter_insideLiteral_ex2 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.acceptsBeginning("a user named"), is(1));
    }
    
    @Test
    public void weightChain_withParameter_beforeParameter_insideLiteral_ex2 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        WeightChain chain = s.calculateWeightChain("a user named");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named")));
    }
    
    @Test
    public void complete_withParameter_beforeParameter_insideLiteral_ex2 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        String complete = s.complete("a user named");
        assertThat(complete, equalTo(" $name clicks"));
    }
    
    @Test
    public void acceptsInputPart_withParameter_beforeParameter_fullLiteral () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.acceptsBeginning("a user named "), is(2));
    }
    
    @Test
    public void weightChain_withParameter_beforeParameter_fullLiteral () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        WeightChain chain = s.calculateWeightChain("a user named ");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ")));
    }
    
    @Test
    public void complete_withParameter_beforeParameter_fullLiteral () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        String complete = s.complete("a user named ");
        assertThat(complete, equalTo("$name clicks"));
    }
    
    @Test
    public void acceptsInputPart_withParameter_beforeParameter_invalidLiteral () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.acceptsBeginning("a user whose "), is(0));
    }
    
    @Test
    public void weightChain_withParameter_beforeParameter_invalidLiteral () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        WeightChain chain = s.calculateWeightChain("a user whose ");
        assertThat(chain.tokenize(), equalTo(Arrays.<String>asList()));
    }
    
    @Test
    public void complete_withParameter_beforeParameter_invalidLiteral () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        String complete = s.complete("a user whose ");
        assertThat(complete, equalTo(""));
    }
    
    @Test
    public void acceptsInputPart_withParameter_insideParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.acceptsBeginning("a user named Travis"), is(3));
    }
    
    @Test
    public void weightChain_withParameter_insideParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        WeightChain chain = s.calculateWeightChain("a user named Travis");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ", "Travis")));
    }
    
    @Test
    public void complete_withParameter_insideParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        String complete = s.complete("a user named Travis");
        assertThat(complete, equalTo(" clicks"));
    }
    
    @Test
    public void acceptsInputPart_withParameter_afterParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.acceptsBeginning("a user named Travis cli"), is(4));
    }
    
    @Test
    public void weightChain_withParameter_afterParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        WeightChain chain = s.calculateWeightChain("a user named Travis cli");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ", "Travis", " cli")));
    }
    
    @Test
    public void complete_withParameter_afterParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        String complete = s.complete("a user named Travis cli");
        assertThat(complete, equalTo("cks"));
    }
    
    @Test
    public void acceptsInputPart_withParameter_fullMatch () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.acceptsBeginning("a user named Travis clicks"), is(5));
    }
    
    @Test
    public void weightChain_withParameter_fullMatch () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        WeightChain chain = s.calculateWeightChain("a user named Travis clicks");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ", "Travis", " clicks")));
    }
    
    @Test
    public void complete_withParameter_fullMatch () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        
        String complete = s.complete("a user named Travis clicks");
        assertThat(complete, equalTo(""));
    }
    
    @Test
    public void weightChain_ex1() {
        ParametrizedStep s = new ParametrizedStep("a user named \"$name\"");
        
        WeightChain chain = s.calculateWeightChain("a user named \"Bob\"");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named \"","Bob","\"")));
    }
    
    @Test
    public void acceptsInputPart_with2Parameters_before2ndParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        assertThat(s.acceptsBeginning("a user named Travis cli"), is(4));
        assertThat(s.acceptsBeginning("a user named Travis clicks on "), is(5));
    }
    
    @Test
    public void weightChain_with2Parameters_before2ndParameter_ex1 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        WeightChain chain = s.calculateWeightChain("a user named Travis cli");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ", "Travis", " cli")));
    }
    
    @Test
    public void weightChain_with2Parameters_before2ndParameter_ex2 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        WeightChain chain = s.calculateWeightChain("a user named Travis clicks on ");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ", "Travis", " clicks on ")));
    }
    
    @Test
    public void complete_with2Parameters_before2ndParameter_ex1 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        String complete = s.complete("a user named Travis cli");
        assertThat(complete, equalTo("cks on $button button"));
    }
    
    @Test
    public void complete_with2Parameters_before2ndParameter_ex2 () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        String complete = s.complete("a user named Travis clicks on ");
        assertThat(complete, equalTo("$button button"));
    }


    @Test
    public void acceptsInputPart_with2Parameters_inside2ndParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        assertThat(s.acceptsBeginning("a user named Travis clicks on enter"), is(6));
    }
    
    @Test
    public void weightChain_with2Parameters_inside2ndParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        WeightChain chain = s.calculateWeightChain("a user named Travis clicks on enter");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ","Travis"," clicks on ", "enter")));
    }
    
    @Test
    public void complete_with2Parameters_inside2ndParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        String complete = s.complete("a user named Travis clicks on enter");
        assertThat(complete, equalTo(" button"));
    }
    
    @Test
    public void acceptsInputPart_with2Parameters_after2ndParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        assertThat(s.acceptsBeginning("a user named Travis clicks on enter butt"), is(7));
    }
    
    @Test
    public void weightChain_with2Parameters_after2ndParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        WeightChain chain = s.calculateWeightChain("a user named Travis clicks on enter butt");
        assertThat(chain.tokenize(), equalTo(Arrays.asList("a user named ", "Travis", " clicks on ", "enter", " butt")));
    }
    
    @Test
    public void complete_with2Parameters_after2ndParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        String complete = s.complete("a user named Travis clicks on enter butt");
        assertThat(complete, equalTo("on"));
    }

    
    @Test
    public void acceptsInputPart_with2Parameters_fullMatch () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        assertThat(s.acceptsBeginning("a user named Travis clicks on enter button"), is(8));
    }
    
    @Test
    public void weightChain_with2Parameters_fullMatch () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        WeightChain chain = s.calculateWeightChain("a user named Travis clicks on enter button");
        List<String> inputFrags = chain.tokenize();
        assertThat(inputFrags, equalTo(Arrays.asList("a user named ", "Travis", " clicks on ", "enter", " button")));
    }
    
    @Test
    public void complete_with2Parameters_fullMatch () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        
        String complete = s.complete("a user named Travis clicks on enter button");
        assertThat(complete, equalTo(""));
    }

    @Test
    public void matches_noParameter () {
        ParametrizedStep s = new ParametrizedStep("a user clicks");
        assertThat(s.matches("a user clicks"), is(true));
        assertThat(s.matches("a user clicks "), is(false));
        assertThat(s.matches("a user named"), is(false));
        assertThat(s.matches("a user clicks on a button"), is(false));
    }
    
    @Test
    public void matches_withOneParameter () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks");
        assertThat(s.matches("a user named Travis clicks"), is(true));
        assertThat(s.matches("a user named $name clicks"), is(true));
        assertThat(s.matches("a user named Travis has clicked"), is(false));
    }
    
    @Test
    public void matches_withTwoParameters () {
        ParametrizedStep s = new ParametrizedStep("a user named $name clicks on $button button");
        assertThat(s.matches("a user named Travis clicks on enter button"), is(true));
        assertThat(s.matches("a user named $name clicks on $butoon button"), is(true));
        assertThat(s.matches("a user named Travis clicks on enter "), is(false));
    }
    
    @Test
    public void weightChain_withNewlines () {
        ParametrizedStep s = new ParametrizedStep("a user named $name with the following properties $exampleTable");
        String content = "a user named Bob with the following properties \n" +
                         "|key|value|\n" +
                         "|Login|networkAgentLogin|\n" +
                         "|Password|networkAgentPassword|\n";
        
        WeightChain chain = s.calculateWeightChain(content);
        List<String> tokenList = chain.tokenize();
        
        //for(String token : tokenList) {
        //    System.out.println(">>"+escapeNL(token)+"<<");
        //}
        assertThat(tokenList.subList(0, 3), equalTo(Arrays.asList("a user named ", "Bob", " with the following properties ")));
        assertThat(tokenList.get(3), equalTo("\n" +
                         "|key|value|\n" +
                         "|Login|networkAgentLogin|\n" +
                         "|Password|networkAgentPassword|\n"));
    }
    
    @Test
    public void weightChain_withNewlines_noParameterBefore () {
        ParametrizedStep s = new ParametrizedStep("a user named Bob with the following properties $exampleTable");
        String content = "a user named Bob with the following properties \n" +
                         "|key|value|\n" +
                         "|Login|networkAgentLogin|\n" +
                         "|Password|networkAgentPassword|\n";
        
        WeightChain chain = s.calculateWeightChain(content);
        List<String> tokenList = chain.tokenize();
        //for(String token : tokenList) {
        //    System.out.println(">>"+escapeNL(token)+"<<");
        //}
        assertThat(tokenList.get(0), equalTo("a user named Bob with the following properties "));
        assertThat(tokenList.get(1), equalTo("\n" +
                         "|key|value|\n" +
                         "|Login|networkAgentLogin|\n" +
                         "|Password|networkAgentPassword|\n"));
    }
}
