package org.jbehave.eclipse.console;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.console.IOConsoleOutputStream;
import org.jbehave.eclipse.Activator;

import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class JBehaveConsoleAppender extends ch.qos.logback.core.UnsynchronizedAppenderBase<ILoggingEvent> {

    private JBehaveConsole console;
    private IOConsoleOutputStream outMessageStream;
    private PatternLayoutEncoder encoder;

    @Override
    public void start() {
        console = JBehaveConsole.findConsole();
        outMessageStream = console.getOutMessageStream();
        try {
            encoder.init(outMessageStream);
        } catch (IOException e) {
            Activator.logError("Failed to initialize encoder on appender start", e);
        }
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    protected void append(final ILoggingEvent event) {
        // Thats probably not the most efficient way, but it works for now
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                try {
                    encoder.doEncode(event);
                } catch (IOException e) {
                    Activator.logError("Failed to encode logging event", e);
                }
            }
        });
    }

    public void setEncoder(PatternLayoutEncoder encoder) {
        this.encoder = encoder;
    }

}
