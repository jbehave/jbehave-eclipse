package org.jbehave.eclipse.editor.story.outline;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;
import org.jbehave.eclipse.editor.story.StoryEditor;

public class OutlineView extends ContentOutlinePage {

    private StoryEditor editor;
    private ImageRegistry imageRegistry;
    private ITextListener textListener;

    public OutlineView(StoryEditor editor, ImageRegistry imageRegistry) {
        this.editor = editor;
        this.imageRegistry = imageRegistry;
    }

    @Override
    public void init(IPageSite pageSite) {
        super.init(pageSite);
        // pageSite.setSelectionProvider(this);
        // IStructuredSelection sel = (IStructuredSelection) pageSite.getWorkbenchWindow().getSelectionService().getSelection();
        
        textListener = new ITextListener() {
            @Override
            public void textChanged(TextEvent event) {
                refresh();
            }
        };
        editor.addTextListener(textListener);
    }
    
    @Override
    public void dispose() {
        editor.outlinePageClosed();
        editor.removeTextListener(textListener);
        super.dispose();
    }

    @Override
    public void createControl(Composite parent) {
        super.createControl(parent);
        TreeViewer treeViewer = getTreeViewer();
        
        // Handle mouse clicks
        treeViewer.getTree().addMouseListener(new MouseAdapter() {
            @Override
             public void mouseDoubleClick(MouseEvent e) {
                gotoSelectedElement();
             }
        });

        // Set the content provider
        QuickOutlineTreeContentProvider treeContentProvider = new QuickOutlineTreeContentProvider();
        treeViewer.setContentProvider(treeContentProvider);

        // Set the label provider
        QuickOutlineStyledLabelProvider labelProvider = new QuickOutlineStyledLabelProvider(imageRegistry);
        treeViewer.setLabelProvider(labelProvider);

        treeViewer.setAutoExpandLevel(1);
        treeViewer.setUseHashlookup(true);
        treeViewer.setInput(editor.getOutlineModels());
    }
    
    protected void gotoSelectedElement() {
        OutlineModel selectedElement = (OutlineModel)getSelectedElement();
        if(selectedElement==null) {
            return;
        }
        editor.showRange(selectedElement.getOffset(), selectedElement.getLength());
    }
    
    protected Object getSelectedElement() {
        TreeViewer treeViewer = getTreeViewer();
        if (treeViewer == null) {
            return null;
        }
        return ((IStructuredSelection) treeViewer.getSelection())
                .getFirstElement();
    }

    public void refresh() {
        if (getTreeViewer() != null) {
            final Control c = getTreeViewer().getControl();
            if (c.isDisposed()) {
                return;
            }
            final Display d = c.getDisplay();
            d.asyncExec(new Runnable() {

                public void run() {
                    if (getTreeViewer().getControl() != null
                            && !getTreeViewer().getControl().isDisposed()) {
                        getTreeViewer().setInput(editor.getOutlineModels());
                    }
                }
            });
        }
    }
}
