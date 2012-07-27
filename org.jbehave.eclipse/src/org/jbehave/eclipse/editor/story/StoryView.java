package org.jbehave.eclipse.editor.story;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.jbehave.eclipse.JBehaveProject;
import org.jbehave.eclipse.JBehaveProjectRegistry;
import org.jbehave.eclipse.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoryView extends ViewPart {

    public static final String ID = "org.jbehave.eclipse.editor.story.StoryView"; //$NON-NLS-1$
    
    private Logger logger = LoggerFactory.getLogger(StoryView.class);
    
    private Label storylanguagelabel;

    public StoryView() {
    }
    
    @Override
    public void init(IViewSite site) throws PartInitException {
        super.init(site);
        site.getWorkbenchWindow().getSelectionService().addSelectionListener(new ISelectionListener() {
            @Override
            public void selectionChanged(IWorkbenchPart part, ISelection selection) {
                if(selection.isEmpty() || !(selection instanceof IStructuredSelection))
                    return;
                
                IStructuredSelection structured = (IStructuredSelection)selection;
                Object firstElement = structured.getFirstElement();
                if(!(firstElement instanceof IAdaptable))
                    return;
                    
                IAdaptable adaptable = (IAdaptable)firstElement;
                IProject project = (IProject) adaptable.getAdapter(IProject.class);
                if(project==null) {
                    logger.warn(">> {} ({})", adaptable, adaptable.getClass());
                    return;
                }
                JBehaveProject jbehaveProject = JBehaveProjectRegistry.get().getProject(project);
                updateProjectInfos(jbehaveProject);
            }
        });
        site.getPage().addPartListener(new IPartListener2() {
            
            @Override
            public void partVisible(IWorkbenchPartReference partRef) {
            }
            
            @Override
            public void partOpened(IWorkbenchPartReference partRef) {
                changeContent(partRef);
            }
            
            @Override
            public void partInputChanged(IWorkbenchPartReference partRef) {
                changeContent(partRef);
            }
            
            @Override
            public void partHidden(IWorkbenchPartReference partRef) {
            }
            
            @Override
            public void partDeactivated(IWorkbenchPartReference partRef) {
            }
            
            @Override
            public void partClosed(IWorkbenchPartReference partRef) {
                clearContent(partRef);
            }
            
            @Override
            public void partBroughtToTop(IWorkbenchPartReference partRef) {
                changeContent(partRef);
            }
            
            @Override
            public void partActivated(IWorkbenchPartReference partRef) {
                changeContent(partRef);                
            }
        });
    }
    
    private void clearContent (IWorkbenchPartReference partRef) {
        if(!isInterestedBy(partRef))
            return;
        storylanguagelabel.setText("n/a");
    }
    
    private void changeContent (IWorkbenchPartReference partRef) {
        if(!isInterestedBy(partRef))
            return;
        StoryEditor editorPart = (StoryEditor)partRef.getPart(true);
        JBehaveProject jBehaveProject = editorPart.getJBehaveProject();
        updateProjectInfos(jBehaveProject);
    }

    protected void updateProjectInfos(JBehaveProject jbehaveProject) {
        if(jbehaveProject==null)
            return;
        storylanguagelabel.setText(jbehaveProject.getLocale().toString());
    }

    private boolean isInterestedBy(IWorkbenchPartReference partRef) {
        return StoryEditor.EDITOR_ID.equals(partRef.getId());
    }

    /**
     * Create contents of the view part.
     * @param parent
     */
    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        {
            Label lblStoryLanguage = new Label(container, SWT.NONE);
            lblStoryLanguage.setFont(SWTResourceManager.getFont("Lucida Grande", 11, SWT.BOLD));
            lblStoryLanguage.setText("Story language");
        }
        {
            storylanguagelabel = new Label(container, SWT.NONE);
            storylanguagelabel.setText("en");
            storylanguagelabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        }

        createActions();
        initializeToolBar();
        initializeMenu();
    }

    /**
     * Create the actions.
     */
    private void createActions() {
        // Create the actions
    }

    /**
     * Initialize the toolbar.
     */
    private void initializeToolBar() {
        //IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
    }

    /**
     * Initialize the menu.
     */
    private void initializeMenu() {
        //IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
    }

    @Override
    public void setFocus() {
        // Set the focus
    }

}
