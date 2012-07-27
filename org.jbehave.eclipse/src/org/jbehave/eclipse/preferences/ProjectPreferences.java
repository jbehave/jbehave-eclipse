package org.jbehave.eclipse.preferences;

import static org.jbehave.eclipse.util.Objects.o;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.jbehave.eclipse.Activator;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProjectPreferences {
    
    private static final String QUALIFIER = Activator.PLUGIN_ID + "/project";
    
    private static final String DEFAULT_PARAMETER_PREFIX = "$";
    public static final String USE_PROJECT_SETTINGS = "use_project_settings";
    public static final String LANGUAGES = "keyword.languages";  
    public static final String LANGUAGE = "keyword.language";
    public static final String PARAMETER_PREFIX = "parameter.prefix";

    //
    private Logger logger = LoggerFactory.getLogger(ProjectPreferences.class);
    //
    private final PreferencesHelper helper;
    //
    private final boolean isProjectLevel;
    private boolean useProjectSettings;
    private String storyLanguage;
    private String parameterPrefix;
    //
    private String[] availableStoryLanguages;

    public ProjectPreferences(IScopeContext scope) {
        helper = PreferencesHelper.getHelper(QUALIFIER, scope);
        isProjectLevel = false;
    }
    
    public ProjectPreferences() {
        helper = PreferencesHelper.getHelper(QUALIFIER);
        isProjectLevel = false;
    }

    public ProjectPreferences(final IProject project) {
        helper = PreferencesHelper.getHelper(QUALIFIER, project);
        isProjectLevel = true;
    }
    
    public void addListener(IPreferenceChangeListener changeListener) {
        helper.addListener(changeListener);
    }
    
    public boolean hasOptionsAtLowestScope() {
        return helper.hasAnyAtLowestScope();
    }

    public void store() throws BackingStoreException {
        helper.removeAllAtLowestScope();
        helper.putString(LANGUAGE, storyLanguage);
        helper.putString(LANGUAGES, StringUtils.join(availableStoryLanguages,","));
        helper.putBoolean(USE_PROJECT_SETTINGS, useProjectSettings);
        helper.putString(PARAMETER_PREFIX, parameterPrefix);
        helper.flush();
        logger.info("Project preferences stored (projectLevel: {}), storyLanguage: {}, useProjectSettings: {}, parameterPrefix: {}",//
                o(isProjectLevel, storyLanguage, useProjectSettings, parameterPrefix));
    }

    public void load() throws BackingStoreException {
        storyLanguage = helper.getString(LANGUAGE, "en");
        availableStoryLanguages = helper.getString(LANGUAGES, "en").split(",");
        useProjectSettings = helper.getBoolean(USE_PROJECT_SETTINGS, false);
        parameterPrefix = helper.getString(PARAMETER_PREFIX, DEFAULT_PARAMETER_PREFIX);
        logger.info("Project preferences loaded (projectLevel: {}), storyLanguage: {}, useProjectSettings: {}, parameterPrefix: {}",//
                o(isProjectLevel, storyLanguage, useProjectSettings, parameterPrefix));
    }
    
    public String[] availableStoryLanguages() {
        return availableStoryLanguages;
    }
    public void setAvailableStoryLanguages(String... availableStoryLanguages) {
        this.availableStoryLanguages = availableStoryLanguages;
    }
    
    public void setStoryLanguage(String storyLanguage) {
        this.storyLanguage = storyLanguage;
    }
    
    public String getStoryLanguage() {
        return storyLanguage;
    }
    
    public boolean isUseProjectSettings() {
        return useProjectSettings;
    }
    
    public void setUseProjectSettings(boolean useProjectSettings) {
        this.useProjectSettings = useProjectSettings;
    }
    
    public String getParameterPrefix() {
    	if ( parameterPrefix == null ){
    		return DEFAULT_PARAMETER_PREFIX;
    	}
        return parameterPrefix;
    }
    
    /**
     * Define the parameter prefix, if the prefix is blank then it is automatically
     * replaced by '$'
     * @param parameterPrefix
     */
    public void setParameterPrefix(String parameterPrefix) {
        if(StringUtils.isBlank(parameterPrefix))
            parameterPrefix = DEFAULT_PARAMETER_PREFIX;
        this.parameterPrefix = parameterPrefix.trim();
    }
    
    public void removeAllSpecificSettings() throws BackingStoreException {
        logger.info("Remove project specific settings");
        helper.removeAllAtLowestScope();
        load();
    }

}
