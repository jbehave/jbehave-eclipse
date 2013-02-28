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

@RunWith(MockitoJUnitRunner.class)
public class MethodToStepCandidateReducerTest {

    @Mock
    private StepCandidateReduceListener listener;

    private MethodToStepCandidateReducer reducer;

    @Mock
    private IMethod method;

    private List<IAnnotation> annotations;

    @Test
    public void testContainerNotInformed_WhenMethodWithoutAnnotation()
	    throws JavaModelException {
	givenNoAnnotation();

	whenTheMethodWasProcessed();

	thenListenerShouldNotHaveBeenInformed();
    }

    @Test
    public void testContainerInformedOnce_WhenOneGivenAnnotation()
	    throws JavaModelException {
	givenAnnotation("Given", "this Given test");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.GIVEN,
		"this Given test", null);
    }

    @Test
    public void testContainerInformedOnce_WhenOneWhenAnnotation()
	    throws JavaModelException {
	givenAnnotation("When", "this When test");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.WHEN,
		"this When test", null);
    }

    @Test
    public void testContainerInformedOnce_WhenOneThenAnnotation()
	    throws JavaModelException {
	givenAnnotation("Then", "this test with Then");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.THEN,
		"this test with Then", null);
    }

    @Test
    public void testContainerInformedWithPriority_WhenProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a priority", 1);

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedOnlyWith(StepType.GIVEN,
		"a priority", 1);
    }

    @Test
    public void testContainerInformedWithFirst_WhenTwoAnnotations()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAnnotation("When", "a user logs in");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is logged in", null);
    }

    @Test
    public void testContainerInformedWithSecond_WhenTwoAnnotations()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAnnotation("When", "a user logs in");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.WHEN, "a user logs in",
		null);
    }

    @Test
    public void testContainerInformedWithAlias_WhenProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAnnotation("Alias", "a user is present");

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is present", 0);
    }

    @Test
    public void testContainerInformedWithBasicStep_WhenAliasesProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAliases(new String[] { "a user is present",
		"a user session exists" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is logged in", null);
    }

    @Test
    public void testContainerInformedWithFirstAlias_WhenAliasesProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAliases(new String[] { "a user is present",
		"a user session exists" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user is present", 0);
    }

    @Test
    public void testContainerInformedWithSecondAlias_WhenAliasesProvided()
	    throws JavaModelException {
	givenAnnotation("Given", "a user is logged in");
	givenAliases(new String[] { "a user is present",
		"a user session exists" });

	whenTheMethodWasProcessed();

	thenListenerShouldHaveBeenInformedWith(StepType.GIVEN,
		"a user session exists", 0);
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
	givenAnnotation(elementName, value, null);
    }

    public void givenAnnotation(String elementName, String value,
	    Integer priority) {
	final List<IMemberValuePair> attributes = new ArrayList<IMemberValuePair>();

	attributes.add(getMemberValuePair("value", value));
	if (priority != null) {
	    attributes.add(getMemberValuePair("priority", priority));
	}

	givenAnnotation(elementName, attributes);
    }

    public void givenAliases(String[] values) {
	final List<IMemberValuePair> attributes = new ArrayList<IMemberValuePair>();

	attributes.add(getMemberValuePair("values", values));
	givenAnnotation("Aliases", attributes);
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

	    @Override
	    public int getValueKind() {
		return 0;
	    }

	    @Override
	    public Object getValue() {
		return value;
	    }

	    @Override
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
