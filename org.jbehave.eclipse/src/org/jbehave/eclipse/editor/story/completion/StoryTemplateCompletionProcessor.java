package org.jbehave.eclipse.editor.story.completion;

import static org.jbehave.eclipse.util.Objects.o;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateCompletionProcessor;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.Keyword;
import org.jbehave.eclipse.util.New;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryTemplateCompletionProcessor extends TemplateCompletionProcessor {

    private Logger logger = LoggerFactory.getLogger(StoryTemplateCompletionProcessor.class);

    private List<Template> additionals = New.arrayList();
    private final JBehaveProject project;

    public StoryTemplateCompletionProcessor() {
        this(null);
    }

    public StoryTemplateCompletionProcessor(JBehaveProject project) {
        super();
        this.project = project;
    }

    public void additional(Template template) {
        additionals.add(template);
    }

    @Override
    protected TemplateContextType getContextType(final ITextViewer viewer, final IRegion region) {
        return StoryContextType.getTemplateContextType();
    }

    @Override
    protected Image getImage(final Template template) {
        return null;
    }

    @Override
    protected Template[] getTemplates(final String contextTypeId) {
        List<Template> list = New.arrayList();
        list.addAll(additionals);

        //
        // TODO this is not the most efficient way, but i didn't manage to add
        // additional information such as locale in the template. Id is the only
        // attribute that is not directly visible, thus the locale is directly
        // a part of it. Allowing us to filter against the current one.
        // Note that the 'usual' default strategy is not used:
        // fr_FR_Traditional will not retrieve fr, fr_FR templates! It must
        // match *exactly*

        Locale locale = null;
        if (project != null)
            locale = project.getLocale();

        final String localeFilter = (locale == null) ? "_en" : "_" + locale.toString();

        TemplateStore templateStore = Activator.getDefault().getTemplateStore();
        for (TemplatePersistenceData data : templateStore.getTemplateData(false)) {
            if (data.isUserAdded()) {
                list.add(data.getTemplate());
            } else if (data.getId().endsWith(localeFilter) || data.getId().endsWith("_shared")) {
                list.add(data.getTemplate());
            }
        }
        logger.debug("Template list retrieved for context type {}, locale {}: #{} templates",
                o(contextTypeId, locale, list.size()));
        return list.toArray(new Template[list.size()]);
    }

    @Override
    protected ICompletionProposal createProposal(Template template, TemplateContext context, IRegion region,
            int relevance) {
        final TemplateProposal p = new TemplateProposal(template, context, region, getImage(template), getRelevance(
                template, "prefix")) {
            @Override
            public String getAdditionalProposalInfo() {
                String content = super.getAdditionalProposalInfo();
                return formatTemplateToHTML(content);
            }
        };
        p.setInformationControlCreator(new IInformationControlCreator() {

            public IInformationControl createInformationControl(final Shell parent) {
                return new DefaultInformationControl(parent, true) {
                    @Override
                    public void setInformation(String content) {
                        super.setInformation(content);
                    }
                };
            }
        });
        return p;
    }

    private static String formatTemplateToHTML(String content) {
        for (Keyword keyword : Keyword.values()) {
            String asString = keyword.asString();
            if (asString.endsWith(":"))
                asString = asString.substring(0, asString.length() - 1);
            String regex = "^(" + Pattern.quote(asString) + ")";
            Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
            content = pattern.matcher(content).replaceAll("<b>$1</b>");
        }
        content = content.replaceAll("[\r\n]+", "<br>");
        return content;
    }

    @Override
    protected TemplateContext createContext(ITextViewer viewer, IRegion region) {
        final TemplateContextType contextType = getContextType(viewer, region);
        if (contextType instanceof StoryContextType) {
            final IDocument document = viewer.getDocument();
            return new JBehaveTemplateContext(contextType, 
                    project,
                    document,
                    region.getOffset(), region.getLength());
        }
        return super.createContext(viewer, region);
    }
}
