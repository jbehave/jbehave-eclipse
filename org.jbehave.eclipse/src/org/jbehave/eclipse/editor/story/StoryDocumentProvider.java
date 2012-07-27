package org.jbehave.eclipse.editor.story;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.JBehaveProjectRegistry;
import org.jbehave.eclipse.Dialogs;
import org.jbehave.eclipse.editor.story.scanner.AllInOnePartitionScanner;
import org.jbehave.eclipse.editor.story.scanner.AllInOneScanner;
import org.jbehave.eclipse.editor.story.scanner.StoryPartitionScanner;
import org.jbehave.eclipse.editor.text.ProjectAwareFastPartitioner;
import org.jbehave.eclipse.util.Strings;

public class StoryDocumentProvider extends FileDocumentProvider {

    protected IDocument createDocument(Object element) throws CoreException {
        if (element instanceof FileEditorInput) {
            final IProject project = ((FileEditorInput) element).getFile().getProject();
            IDocument document = super.createDocument(element);
            if (document != null) {
                JBehaveProject jbehaveProject = getJBehaveProject(project);
                ((StoryDocument)document).setJBehaveProject(jbehaveProject);
                IDocumentPartitioner partitioner = createPartitioner(jbehaveProject);
                partitioner.connect(document);
                document.setDocumentPartitioner(partitioner);
            }
            return document;
        }
        // TODO: what if it is not a file?
        Dialogs.warning("Unsupported type", "Cannot open the following type: " + element.getClass());
        return null;
    }

    protected JBehaveProject getJBehaveProject(final IProject project) {
        return JBehaveProjectRegistry.get().getOrCreateProject(project);
    }
    
    @Override
    protected IDocument createEmptyDocument() {
        return new StoryDocument();
    }

    private IDocumentPartitioner createPartitioner(final JBehaveProject jbehaveProject) {
        List<String> names = new ArrayList<String>();
        if(AllInOneScanner.allInOne) {
            names.add(IDocument.DEFAULT_CONTENT_TYPE);
            return new ProjectAwareFastPartitioner(
                    new AllInOnePartitionScanner(),
                    Strings.toArray(names), jbehaveProject.getProject());
        }
        else {
            names.addAll(StoryPartition.names());
            names.add((String)TokenConstants.IGNORED.getData());
            return new ProjectAwareFastPartitioner(
                    new StoryPartitionScanner(jbehaveProject),
                    Strings.toArray(names), jbehaveProject.getProject());
        }
    }

}
