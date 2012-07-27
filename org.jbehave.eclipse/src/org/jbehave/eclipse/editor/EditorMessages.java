package org.jbehave.eclipse.editor;

import java.util.ResourceBundle;

public class EditorMessages {
    private static final String BUNDLE_FOR_CONSTRUCTED_KEYS = "org.jbehave.eclipse.editor.ConstructedEditorMessages";//$NON-NLS-1$

    private static final ResourceBundle fgBundleForConstructedKeys = ResourceBundle
            .getBundle(BUNDLE_FOR_CONSTRUCTED_KEYS);

    /**
     * Returns the message bundle which contains constructed keys.
     * 
     * @since 3.1
     * @return the message bundle
     */
    public static ResourceBundle getBundleForConstructedKeys() {
        return fgBundleForConstructedKeys;
    }
}
