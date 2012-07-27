package org.jbehave.eclipse.editor.story.completion;

import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.Keyword;

public class StoryContextType extends TemplateContextType {

    /** This context's id */
    public static final String STORY_CONTEXT_TYPE_ID = "org.jbehave.jbehave.story"; //$NON-NLS-1$

    /**
     * Creates a new XML context type.
     */
    public StoryContextType() {
        addGlobalResolvers();
    }

    private void addGlobalResolvers() {
        addResolver(new GlobalTemplateVariables.Cursor());
        addResolver(new GlobalTemplateVariables.WordSelection());
        addResolver(new GlobalTemplateVariables.LineSelection());
        addResolver(new GlobalTemplateVariables.Dollar());
        addResolver(new GlobalTemplateVariables.Date());
        addResolver(new GlobalTemplateVariables.Year());
        addResolver(new GlobalTemplateVariables.Time());
        addResolver(new GlobalTemplateVariables.User());
        for(Keyword keyword : Keyword.values()) {
            addResolver(new LocalizedKeywordResolver(keyword));
        }
    }

    public static TemplateContextType getTemplateContextType() {
        return Activator.getDefault().getContextTypeRegistry()
                .getContextType(StoryContextType.STORY_CONTEXT_TYPE_ID);
    }
    
}
