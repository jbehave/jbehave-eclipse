package org.jbehave.eclipse.editor.story;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.StepJumper;
import org.jbehave.eclipse.parser.RegexUtils;
import org.jbehave.eclipse.parser.StoryElement;
import org.jbehave.eclipse.util.Ref;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StepHyperLinkDetector implements IHyperlinkDetector {

    private static Logger logger = LoggerFactory.getLogger(StepHyperLinkDetector.class);

    private IHyperlink[] NONE = null;// new IHyperlink[0];

    @Override
    public IHyperlink[] detectHyperlinks(final ITextViewer viewer, final IRegion region,
            boolean canShowMultipleHyperlinks) {

        logger.debug("Searching for hyperlink in region offset: {}, length: {}", region.getOffset(), region.getLength());

        IDocument document = viewer.getDocument();
        if (!(document instanceof StoryDocument)) {
            logger.error("Document is not a story document got: {}, hyperlink detector failed", document.getClass());
            return NONE;
        }
        StoryDocument storyDocument = (StoryDocument) document;
        final JBehaveProject jbehaveProject = storyDocument.getJBehaveProject();
        LocalizedStepSupport localizedStepSupport = jbehaveProject.getLocalizedStepSupport();

        final Ref<StoryElement> found = new StoryDocumentUtils(localizedStepSupport).findStoryElementAtRegion(document,
                region);
        if (found.isNull()) {
            logger.debug("No story part found in region offset: {}, length: {}", region.getOffset(), region.getLength());
            return NONE;
        }

        final StoryElement part = found.get();
        if (!part.isStep()) {
            logger.debug("Part found is not a step part got: {}", part.extractKeyword());
            return NONE;
        }
        final String step = part.stepWithoutKeyword();
        final String partCleaned = RegexUtils.removeTrailingComment(part.getContent());
        IHyperlink link = new IHyperlink() {

            @Override
            public IRegion getHyperlinkRegion() {
                return new Region(part.getOffset(), partCleaned.length());
            }

            @Override
            public String getHyperlinkText() {
                return step;
            }

            @Override
            public String getTypeLabel() {
                return "Go to step";
            }

            @Override
            public void open() {
                try {
                    new StepJumper(jbehaveProject).jumpToDeclaration(viewer, step);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
        return new IHyperlink[] { link };
    }

}
