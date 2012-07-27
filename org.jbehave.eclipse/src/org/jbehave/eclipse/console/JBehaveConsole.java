package org.jbehave.eclipse.console;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.eclipse.ui.part.IPageBookViewPage;

public class JBehaveConsole extends org.eclipse.ui.console.IOConsole {

    private IOConsoleOutputStream outMessageStream;
    private IOConsoleOutputStream errMessageStream;

    private static final String NAME = "JBehave";

    public JBehaveConsole() {
        super(NAME, null);
        outMessageStream = newOutputStream();
        outMessageStream.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        errMessageStream = newOutputStream();
        errMessageStream.setColor(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
    }
    
    public IOConsoleOutputStream getOutMessageStream() {
        return outMessageStream;
    }
    
    @Override
    public IPageBookViewPage createPage(IConsoleView view) {
        return super.createPage(view);
    }
    
    public static JBehaveConsole findConsole() {
        ConsolePlugin plugin = ConsolePlugin.getDefault();
        IConsoleManager conMan = plugin.getConsoleManager();
        IConsole[] existing = conMan.getConsoles();
        for (int i = 0; i < existing.length; i++) {
            if (NAME.equals(existing[i].getName()))
                return (JBehaveConsole) existing[i];
        }
        // no console found, so create a new one
        JBehaveConsole myConsole = new JBehaveConsole();
        conMan.addConsoles(new IConsole[] { myConsole });
        return myConsole;
    }

}
