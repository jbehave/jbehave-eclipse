package org.jbehave.eclipse.editor.story.outline;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.story.StoryEditor;
import org.jbehave.eclipse.editor.story.outline.QuickOutlinePopupDialog;
import org.jbehave.eclipse.util.New;

public class QuickOutlinePopupDialogMain {

	public static void main(String[] args) {
        List<StepCandidate> steps = New.arrayList();
        steps.add(new StepCandidate(null, "$", null, StepType.GIVEN, "a user named $username", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.WHEN, "the user's firstname is changed to $firstname", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.WHEN, "the user's lastname is changed to $lastname", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.WHEN, "the user's login is changed to $login", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.WHEN, "the user clicks on $button button", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.THEN, "the '$password' field becomes '$color'", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.THEN, "the page title is '$title'", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.THEN, "the user is logged", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.GIVEN, "the account balance is $amount", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.GIVEN, "the card is valid", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.GIVEN, "the machine contains enough money", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.WHEN, "the Account Holder requests $amount", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.THEN, "the ATM should not dispense any money", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.THEN, "the ATM should say there are insufficient funds", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.THEN, "the account balance should be $amount", 0));
        steps.add(new StepCandidate(null, "$", null, StepType.THEN, "the card should be returned", 0));

        final StoryEditor editor = mock(StoryEditor.class);
        when(editor.getStepCandidates()).thenReturn(steps);
        
        final Display display = new Display();
        final Shell shell = new Shell(display, SWT.DIALOG_TRIM);
        
        final ImageRegistry imageRegistry = new ImageRegistry(display);

        Button button = new Button(shell, SWT.PUSH);
        button.setText("Press to see the InfoPopup");
        button.setBounds(90, 10, 200, 30);

        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent se) {
                // You can set the size of the Rectangle
                new QuickOutlinePopupDialog(new Shell(), SWT.NONE, editor, imageRegistry).open();
            }
        });
        shell.setSize(400, 200);
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        display.dispose();
    }
    
}
