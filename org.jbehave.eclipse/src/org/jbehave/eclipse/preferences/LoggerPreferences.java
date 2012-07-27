package org.jbehave.eclipse.preferences;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.jbehave.eclipse.Activator;
import org.osgi.service.prefs.BackingStoreException;

import ch.qos.logback.classic.Level;
import fj.F2;
import fj.data.List;
import fj.data.Option;

public class LoggerPreferences {
    public static final String QUALIFIER = Activator.PLUGIN_ID + "/logger";
    //
    public static final String USE_PROJECT_SETTINGS = "use_project_settings";
    public static final String INLINED_ENTRIES = "inlined_entries";
    private static final String SEPARATOR = ";";
    
    private final PreferencesHelper helper;
    private List<LoggerEntry> entries = List.nil();
    private boolean useProjectSettings;

    public LoggerPreferences(IScopeContext scope) {
        helper = PreferencesHelper.getHelper(QUALIFIER, scope);
    }
    
    public LoggerPreferences() {
        helper = PreferencesHelper.getHelper(QUALIFIER);
    }

    public LoggerPreferences(final IProject project) {
        helper = PreferencesHelper.getHelper(QUALIFIER, project);
    }
    
    public boolean hasOptionsAtLowestScope() {
        return helper.hasAnyAtLowestScope();
    }
    
    public List<LoggerEntry> getLoggerEntries() {
        return entries;
    }

    public void store() throws BackingStoreException {
        helper.removeAllAtLowestScope();
        StringBuilder inlinedEntries = entries.foldLeft(inlineEntries(), new StringBuilder());
        helper.putString(INLINED_ENTRIES, inlinedEntries.toString());
        helper.putBoolean(USE_PROJECT_SETTINGS, useProjectSettings);
        Activator.logInfo("Storing logger settings: [" + inlinedEntries + "]");
        helper.flush();
        
        // This is ugly, but property change listener are not fired... 
        // need to investigate, in the meanwhile, do it manually!
        Activator.getDefault().resetLoggerLevels();
    }
    
    public void load() throws BackingStoreException {
        entries = List.nil();
        String inlinedEntries = helper.getString(INLINED_ENTRIES, "");
        Activator.logInfo("Loading logger settings: [" + inlinedEntries + "]");
        for(String entryAsString : inlinedEntries.split(Pattern.quote(SEPARATOR))) {
            String[] frags = entryAsString.split(":");
            if(frags.length<2)
                // oops!
                continue;
            entries = entries.snoc(LoggerEntry.newEntry(frags[0], frags[1]));
        }
        useProjectSettings = helper.getBoolean(USE_PROJECT_SETTINGS, false);
    }

    protected static F2<StringBuilder, LoggerEntry, StringBuilder> inlineEntries() {
        return new F2<StringBuilder,LoggerEntry,StringBuilder>() {
            @Override
            public StringBuilder f(StringBuilder out, LoggerEntry entry) {
                out.append(entry.getLoggerName()).append(":").append(entry.getLevel().levelStr).append(SEPARATOR);
                return out;
            }
        };
    }
    
    public boolean isUseProjectSettings() {
        return useProjectSettings;
    }
    
    public void setUseProjectSettings(boolean useProjectSettings) {
        this.useProjectSettings = useProjectSettings;
    }
    
    public void removeAllSpecificSettings() throws BackingStoreException {
        helper.removeAllAtLowestScope();
        load();
    }

    public Object[] getEntriesAsObjectArray() {
        return entries.toCollection().toArray();
    }

    public void removeEntry(LoggerEntry entry) {
        entries = entries.removeAll(entry.sameNameF());
    }

    public void addEntry(String loggerName, Level level) {
        LoggerEntry entry = new LoggerEntry(loggerName, level);
        Option<LoggerEntry> found = entries.find(entry.sameNameF());
        if(found.isSome())
            removeEntry(entry);
        entries = entries.snoc(entry);
    }

}
