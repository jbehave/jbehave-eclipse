package org.jbehave.eclipse.preferences;

import java.util.ResourceBundle;

public class PreferencesMessages {
    private static final String BUNDLE = "org.jbehave.eclipse.preferences.PreferencesMessages";//$NON-NLS-1$

    private static final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE);

    /**
     * Returns the message bundle which contains constructed keys.
     * 
     * @since 3.1
     * @return the message bundle
     */
    public static ResourceBundle getBundle() {
        return bundle;
    }
}
