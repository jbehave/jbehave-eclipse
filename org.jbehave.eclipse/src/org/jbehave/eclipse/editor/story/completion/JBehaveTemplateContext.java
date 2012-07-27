package org.jbehave.eclipse.editor.story.completion;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;
import org.jbehave.eclipse.JBehaveProject;

public class JBehaveTemplateContext extends DocumentTemplateContext {
    
    private final JBehaveProject project;

    public JBehaveTemplateContext(final TemplateContextType contextType,
            final JBehaveProject project,
            final IDocument document, 
            final int offset, 
            final int length) {
        super(contextType, document, offset, length);
        this.project = project;
    }

    @Override
    public boolean canEvaluate(Template template) {
        return true;
    }
    
    @Override
    public TemplateBuffer evaluate(Template template) throws BadLocationException, TemplateException {
        if (!canEvaluate(template)) {
            return null;
        }
        final TemplateTranslator translator = new TemplateTranslator();
        final TemplateBuffer buffer = translator.translate(template);

        getContextType().resolve(buffer, this);

        return buffer;
    }
    
    public JBehaveProject getProject() {
        return project;
    }

}
