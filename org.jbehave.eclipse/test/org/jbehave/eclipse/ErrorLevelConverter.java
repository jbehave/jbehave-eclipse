package org.jbehave.eclipse;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class ErrorLevelConverter extends ClassicConverter {

    @Override
    public String convert(ILoggingEvent event) {
        StringBuffer sbuf = new StringBuffer();
        Level level = event.getLevel();
        sbuf.append(getPrefix(level));
        sbuf.append(level);
        for(int i=level.levelStr.length();i<5;i++)
            sbuf.append(' ');
        sbuf.append(getPrefix(level));
        return sbuf.toString();
    }

    /**
     * Returns the appropriate characters to change the color for the specified logging level.
     */
    private String getPrefix(Level level) {
        switch (level.toInt()) {
            case Level.ERROR_INT:
                return "*";
            case Level.WARN_INT:
                return "!";
            default:
                return " ";
        }
    }

}
