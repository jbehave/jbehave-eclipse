package org.jbehave.eclipse.preferences;

import static org.jbehave.eclipse.preferences.ClassScannerFilterEntry.filter;
import static org.jbehave.eclipse.preferences.ClassScannerFilterEntry.toPatterns;
import static org.jbehave.eclipse.preferences.ClassScannerFilterEntry.toSplittedPatterns;
import static org.jbehave.eclipse.util.FJ.listCollector;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Adler32;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.preferences.ClassScannerFilterEntry.ApplyOn;
import org.jbehave.eclipse.util.Bytes;
import org.jbehave.eclipse.util.StringMatcher;
import org.osgi.service.prefs.BackingStoreException;

import fj.Effect;
import fj.data.List;

public class ClassScannerPreferences {
    private static final String QUALIFIER = Activator.PLUGIN_ID + "/classScanner";
    
    public static final String EXCLUDE_SUFFIX = "-excludes";
    public static final String INCLUDE_SUFFIX = "-includes";
    //
    public static final String USE_PROJECT_SETTINGS = "use_project_settings";
    
    private final PreferencesHelper helper;
    private List<ClassScannerFilterEntry> entries = List.nil();
    private boolean useProjectSettings;

    public ClassScannerPreferences(IScopeContext scope) {
        helper = PreferencesHelper.getHelper(QUALIFIER, scope);
    }
    
    public ClassScannerPreferences() {
        helper = PreferencesHelper.getHelper(QUALIFIER);
    }

    public ClassScannerPreferences(final IProject project) {
        helper = PreferencesHelper.getHelper(QUALIFIER, project);
    }
    
    public void addListener(IPreferenceChangeListener changeListener) {
        helper.addListener(changeListener);
    }
    
    public StringMatcher getPackageRootMatcher() {
        StringMatcher m = new StringMatcher();
        m.addGlobExcludes(getPackageRootExcludes());
        m.addGlobIncludes(getPackageRootIncludes());
        return m;
    }
    
    public String[] getPackageRootExcludes() {
        return getSplittedStrings(ApplyOn.PackageRoot, true);
    }

    public String[] getPackageRootIncludes() {
        return getSplittedStrings(ApplyOn.PackageRoot, false);
    }

    public StringMatcher getPackageMatcher() {
        StringMatcher m = new StringMatcher();
        m.addGlobExcludes(getPackageExcludes());
        m.addGlobIncludes(getPackageIncludes());
        return m;
    }
    
    public String[] getPackageExcludes() {
        return getSplittedStrings(ApplyOn.Package, true);
    }

    public String[] getPackageIncludes() {
        return getSplittedStrings(ApplyOn.Package, false);
    }
    
    public StringMatcher getClassMatcher() {
        StringMatcher m = new StringMatcher();
        m.addGlobExcludes(getClassExcludes());
        m.addGlobIncludes(getClassIncludes());
        return m;
    }

    public String[] getClassExcludes() {
        return getSplittedStrings(ApplyOn.Class, true);
    }

    public String[] getClassIncludes() {
        return getSplittedStrings(ApplyOn.Class, false);
    }
    
    private String[] getSplittedStrings(ApplyOn applyOn, boolean exclude) {
        List<String> patterns = entries
            .filter(filter(applyOn, exclude))
            .map(toSplittedPatterns())
            .foldLeft(listCollector(String.class), List.<String>nil());
        return patterns.toArray().array(String[].class);
    }

    public boolean hasOptionsAtLowestScope() {
        return helper.hasAnyAtLowestScope();
    }

    public void store() throws BackingStoreException {
        helper.removeAllAtLowestScope();
        boolean[] modes = { true, false };
        
        for(ApplyOn applyOn : ApplyOn.values()) {
            for(Boolean mode : modes) {
                List<String> patternsList = entries.filter(filter(applyOn, mode)).map(toPatterns());
                String value = StringUtils.join(patternsList.toCollection(), "|");
                String key = applyOn +  (mode?EXCLUDE_SUFFIX:INCLUDE_SUFFIX);
                helper.putString(key, value);
            }
        }
        helper.putBoolean(USE_PROJECT_SETTINGS, useProjectSettings);
        helper.flush();
    }

    public void load() throws BackingStoreException {
        boolean[] modes = { true, false };

        entries = List.nil();
        for(ApplyOn applyOn : ApplyOn.values()) {
            for(Boolean mode : modes) {
                String key = applyOn +  (mode?EXCLUDE_SUFFIX:INCLUDE_SUFFIX);
                String inlinedPatterns = helper.getString(key, "");
                for(String patterns : inlinedPatterns.split("[\\|]")) {
                    if(StringUtils.isBlank(patterns))
                        continue;
                    entries = entries.snoc(new ClassScannerFilterEntry(patterns, applyOn, mode));
                }
            }
        }
        useProjectSettings = helper.getBoolean(USE_PROJECT_SETTINGS, false);
    }
    
    public ClassScannerFilterEntry addEntry(String patterns, ApplyOn applyOn, boolean exclude) {
        ClassScannerFilterEntry entry = new ClassScannerFilterEntry(patterns, applyOn, exclude);
        if(entries.exists(entry.equalF()))
            return null;
        entries = entries.snoc(entry);
        return entry;
    }
    
    public void removeEntry(ClassScannerFilterEntry entry) {
        entries = entries.removeAll(entry.equalF());
    }

    public List<ClassScannerFilterEntry> getEntries() {
        return entries;
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

    public byte[] calculateHash() {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            entries.foreach(new Effect<ClassScannerFilterEntry>() {
                final byte[] bytes = new byte[2];
                @Override
                public void e(ClassScannerFilterEntry entry) {
                    bytes[0] = (byte) (entry.getApplyOn().ordinal());
                    bytes[1] = (byte) (entry.isExclude()?1:0);
                    md.update(bytes);
                    md.update(entry.getPatterns().getBytes());
                }
            });
            return md.digest();
        }
        catch (NoSuchAlgorithmException e1) {
            final Adler32 adler32 = new Adler32();
            entries.foreach(new Effect<ClassScannerFilterEntry>() {
                final byte[] bytes = new byte[2];
                @Override
                public void e(ClassScannerFilterEntry entry) {
                    bytes[0] = (byte) (entry.getApplyOn().ordinal());
                    bytes[1] = (byte) (entry.isExclude()?1:0);
                    adler32.update(bytes);
                    adler32.update(entry.getPatterns().getBytes());
                }
            });
            return Bytes.longToBytes(adler32.getValue());
        }
    }

}
