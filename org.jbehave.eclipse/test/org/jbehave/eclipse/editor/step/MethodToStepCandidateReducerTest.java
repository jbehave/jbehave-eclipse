package org.jbehave.eclipse.editor.step;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.steps.StepType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import static java.util.Arrays.asList;

@RunWith(MockitoJUnitRunner.class)
public class MethodToStepCandidateReducerTest {

    @Mock
    private StepCandidateReduceListener listener;

    private MethodToStepCandidateReducer reducer;

    @Mock
    private IMethod method;

    private List<IAnnotation> annotations;

    @Test
    public void testListenerNotInformed_WhenMethodWithoutAnnotation()
	    throws JavaModelException {
	givenNoAnnotation();

	whenTheMethodWasProcessed();

	thenListenerShouldNotHaveBeenInformed();
    }

    @Test
    public void testListenerInformedOnce_WhenOneGivenAnnotation()
	    throws JavaModelException {
	givenAnnotation("Given", "this Given test");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.GIVEN,
		"this Given test", null);
    }

    @Test
    public void testListenerInformedOnce_WhenOneWhenAnnotation()
	    throws JavaModelException {
	givenAnnotation("When", "this When test");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.WHEN,
		"this When test", null);
    }

    @Test
    public void testListenerInformedOnce_WhenOneThenAnnotation()
	    throws JavaModelException {
	givenAnnotation("Then", "this test with Then");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.THEN,
		"this test with Then", null);
    }

    @Test
    public void testListenerInformedWithPriority_WhenProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a priority", 1);

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.GIVEN,
		"a priority", 1);
    }

    @Test
    public void testListenerNotInformedWithPriority_WhenNotRecognized()
	    throws JavaModelException {
	givenAnnotation("Given", "a priority", "PRIORITY");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.GIVEN,
		"a priority", null);
    }

    @Test
    public void testListenerInformedWithFirst_WhenTwoAnnotations()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAnnotation("When", "a user logs in");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is logged in", null);
    }

    @Test
    public void testListenerInformedWithSecond_WhenTwoAnnotations()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAnnotation("When", "a user logs in");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.WHEN, "a user logs in",
		null);
    }

    @Test
    public void testListenerInformedWithAlias_WhenProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAnnotation("Alias", "a user is present");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is present", 0);
    }

    @Test
    public void testListenerInformedWithBasicStep_WhenAliasesProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAliases(new String[] { "a user is present",
		"a user session exists" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is logged in", null);
    }

    @Test
    public void testListenerInformedWithFirstAlias_WhenAliasesProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAliases(new String[] { "a user is present",
		"a user session exists" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is present", 0);
    }

    @Test
    public void testListenerInformedWithSecondAlias_WhenAliasesProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAliases(new String[] { "a user is present",
		"a user session exists" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user session exists", 0);
    }

    @Test
    public void testListenerInformed_WhenAnnotationWithFullQualifiedName()
	    throws JavaModelException {
	givenAnnotation("org.jbehave.core.annotations.Given",
		"a fully qualified annotation");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.GIVEN,
		"a fully qualified annotation", null);
    }

    @Test
    public void testListenerInformedWithFirstVariant_WhenPassedInSimpleStep()
	    throws JavaModelException {
	givenAnnotation("When", "a {nice|simple} variant is used");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.WHEN,
		"a nice variant is used", null);
    }

    @Test
    public void testListenerInformedWithSecondVariant_WhenPassedInSimpleStep()
	    throws JavaModelException {
	givenAnnotation("When", "a {nice|simple} variant is used");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.WHEN,
		"a simple variant is used", null);
    }

    @Test
    public void testListenerInformedWithVariant_WhenPassedInAlias()
	    throws JavaModelException {
	givenAnnotation("When", "a variant is used");
	givenAnnotation("Alias", "a {second|third} variant is used");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.WHEN,
		"a second variant is used", 0);
    }

    @Test
    public void testListenerInformedWithVariant_WhenPassedInAliases()
	    throws JavaModelException {
	givenAnnotation("Then", "a variant is possible");
	givenAliases(new String[] { "a {final|test} variant is allowed" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.THEN,
		"a final variant is allowed", 0);
    }

    @Test
    public void testListenerInformedWithAGivenStep_WhenAliasIsFirst()
	    throws JavaModelException {
	givenAnnotation("Alias", "the Given step is missing");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"the Given step is missing", 0);
    }

    @Test
    public void testListenerInformedWithAWhenStep_WhenTheWhenHasAliasAndIsAfterAlias()
	    throws JavaModelException {
	givenAnnotation("Alias", "the Given step is missing");
	givenAnnotation("When", "the it also has a When");
	givenAnnotation("Alias", "it is getting confusing");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.WHEN,
		"it is getting confusing", 0);
    }

    @Test
    public void testListenerInformedWithAGivenStep_WhenAliasesAreFirst()
	    throws JavaModelException {
	givenAliases(new String[] { "a Given step still missing" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a Given step still missing", 0);
    }

    @Before
    public void setUp() throws Exception {
	this.reducer = new MethodToStepCandidateReducer();
	this.annotations = new ArrayList<IAnnotation>();

	Mockito.when(method.getElementName()).thenReturn("testMethod");
    }

    public void givenNoAnnotation() {

    }

    public void givenAnnotation(String elementName, String value) {
	final List<IMemberValuePair> attributes = new ArrayList<IMemberValuePair>();

	attributes.add(getMemberValuePair("value", value));

	givenAnnotation(elementName, getMemberValuePair("value", value));
    }

    public void givenAnnotation(String elementName, String value,
	    Object priority) {
	final List<IMemberValuePair> attributes = new ArrayList<IMemberValuePair>();

	attributes.add(getMemberValuePair("value", value));
	attributes.add(getMemberValuePair("priority", priority));

	givenAnnotation(elementName,
	    getMemberValuePair("value", value),
	    getMemberValuePair("priority", priority));
    }

    public void givenAliases(String[] values) {
	final List<IMemberValuePair> attributes = new ArrayList<IMemberValuePair>();

	attributes.add(getMemberValuePair("values", values));
	givenAnnotation("Aliases", attributes);
    }

    private void givenAnnotation(String elementName,
	    IMemberValuePair... attributes) {
	givenAnnotation(elementName, asList(attributes));
	}

    private void givenAnnotation(String elementName,
	    List<IMemberValuePair> attributes) {
	final IAnnotation annotation = Mockito.mock(IAnnotation.class);

	this.annotations.add(annotation);

	Mockito.when(annotation.getElementName()).thenReturn(elementName);
	try {
	    Mockito.when(annotation.getMemberValuePairs()).thenReturn(
		    attributes.toArray(new IMemberValuePair[0]));
	} catch (JavaModelException e) {
	    e.printStackTrace();
	}
    }

    private IMemberValuePair getMemberValuePair(final String name,
	    final Object value) {
	return new IMemberValuePair() {
	    public int getValueKind() {
	    	return 0;
	    }

	    public Object getValue() {
	    	return value;
	    }

	    public String getMemberName() {
	    	return name;
	    }
	};
    }

    private void whenTheMethodWasProcessed() throws JavaModelException {
	try {
	    Mockito.when(method.getAnnotations()).thenReturn(
		    this.annotations.toArray(new IAnnotation[0]));
	} catch (JavaModelException e) {
	    e.printStackTrace();
	}

	this.reducer.reduce(this.method, this.listener);
    }

    private void thenListenerShouldNotHaveBeenInformed() {
	Mockito.verify(this.listener, Mockito.never()).add(Mockito.eq(method),
		Mockito.any(StepType.class), Mockito.any(String.class),
		Mockito.any(Integer.class));
    }

    private void thenListenerShouldHaveBeenInformedOnlyWith(StepType stepType,
	    String stepPattern, Integer priority) {
	Mockito.verify(this.listener, Mockito.only()).add(method, stepType,
		stepPattern, priority);
    }

    private void thenListenerShouldHaveBeenInformedWith(StepType stepType,
	    String stepPattern, Integer priority) {
	Mockito.verify(this.listener).add(method, stepType, stepPattern,
		priority);
    }
}
