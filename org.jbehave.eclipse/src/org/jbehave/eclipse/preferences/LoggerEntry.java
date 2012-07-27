package org.jbehave.eclipse.preferences;

import org.codehaus.plexus.util.StringUtils;

import ch.qos.logback.classic.Level;
import fj.F;

public final class LoggerEntry {
    
    public static LoggerEntry newEntry(String loggerName, String levelAsString) {
        return new LoggerEntry(loggerName, Level.valueOf(levelAsString));
    }
    
    private final String loggerName;
    private final Level level;
    public LoggerEntry(String loggerName, Level level) {
        super();
        this.loggerName = loggerName;
        this.level = level;
    }
    /**
     * @return the loggerName
     */
    public String getLoggerName() {
        return loggerName;
    }
    /**
     * @return the level
     */
    public Level getLevel() {
        return level;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((level == null) ? 0 : level.toInt());
        result = prime * result + ((loggerName == null) ? 0 : loggerName.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        LoggerEntry other = (LoggerEntry) obj;
        if (level == null) {
            if (other.level != null)
                return false;
        } else if (level.toInt()!=other.level.toInt())
            return false;
        if (loggerName == null) {
            if (other.loggerName != null)
                return false;
        } else if (!loggerName.equals(other.loggerName))
            return false;
        return true;
    }
    
    public F<LoggerEntry, Boolean> sameNameF() {
        return new F<LoggerEntry, Boolean>() {
            @Override
            public Boolean f(LoggerEntry entry) {
                return StringUtils.equals(loggerName, entry.getLoggerName());
            }
        };
    }
    
}
