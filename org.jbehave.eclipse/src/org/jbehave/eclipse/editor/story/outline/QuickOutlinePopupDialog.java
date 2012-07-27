package org.jbehave.eclipse.editor.story.outline;

import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.jbehave.eclipse.editor.story.PatternViewFilter;
import org.jbehave.eclipse.editor.story.StoryEditor;
import org.jbehave.eclipse.editor.text.TextProvider;
import org.jbehave.eclipse.util.Strings;

public class QuickOutlinePopupDialog extends PopupDialog {

    private ImageRegistry imageRegistry;
    
    /**
     * The text control that displays the text.
     */
    private TreeViewer treeViewer;
    private PatternViewFilter fNamePatternFilter;
    private QuickOutlineTreeContentProvider treeContentProvider;
    private QuickOutlineStyledLabelProvider labelProvider;
    private StoryEditor editor;
    private Text filterText;

    public QuickOutlinePopupDialog(Shell parent, int shellStyle, StoryEditor editor, ImageRegistry imageRegistry) {
        super(parent, shellStyle, true, true, true, true, true, null, null);
        setInfoText("select a value or press ESC to exit");
        this.editor = editor;
        this.imageRegistry = imageRegistry;
    }
    
    public void setFocus() {
        getShell().forceFocus();
        filterText.setFocus();
    }
    
    @Override
    protected Control getFocusControl() {
        return filterText;
    }

    /*
     * Create a text control for showing the info about a proposal.
     */
    protected Control createDialogArea(Composite parent) {
        /*
         * text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP |
         * SWT.NO_FOCUS); text.setText(contents); return text;
         */

        // Create the tree viewer
        createUIWidgetTreeViewer(parent);
        
        // Add listeners to the tree viewer
        createUIListenersTreeViewer();
        
        //
        createUIActions();
        
        // Return the tree
        return treeViewer.getControl();
    }
    
    /**
    *
    */
   private void createUIActions() {
   }
   
    /**
     * @param parent
     */
    private void createUIWidgetTreeViewer(final Composite parent) {
        final int style = SWT.H_SCROLL | SWT.V_SCROLL;

        // Create the tree
        final Tree widget = new Tree(parent, style);

        // Configure the layout
        final GridData data = new GridData(GridData.FILL_BOTH);
        data.heightHint = widget.getItemHeight() * 12;
        widget.setLayoutData(data);

        // Create the tree viewer
        treeViewer = new TreeViewer(widget);

        // Add the name pattern filter
        fNamePatternFilter = new PatternViewFilter();
        treeViewer.addFilter(fNamePatternFilter);

        // Set the content provider
        treeContentProvider = new QuickOutlineTreeContentProvider();
        treeViewer.setContentProvider(treeContentProvider);

        // Set the label provider
        labelProvider = new QuickOutlineStyledLabelProvider(imageRegistry);
        treeViewer.setLabelProvider(labelProvider);

        // Create the outline sorter (to be set on the sort action)
        // fTreeViewerComparator = fOutlineContentCreator
        // .createOutlineComparator();
        // Set the comparator to null (sort action will be disabled initially
        // because of this)
        // Create the default outline sorter (Most like this will just return
        // null to indicate sorting disabled
        // fTreeViewerDefaultComparator = fOutlineContentCreator
        // .createDefaultOutlineComparator();
        // fTreeViewer.setComparator(fTreeViewerDefaultComparator);
        treeViewer.setAutoExpandLevel(1);
        treeViewer.setUseHashlookup(true);
        treeViewer.setInput(editor.getOutlineModels());
    }
    
    /**
    *
    */
   private void createUIListenersTreeViewer() {
       // Get the underlying tree widget
       final Tree tree = treeViewer.getTree();
       // Handle key events
       tree.addKeyListener(new KeyListener() {
           public void keyPressed(final KeyEvent e) {
               if (e.character == 0x1B) {
                   // Dispose on ESC key press
                   close();
               }
           }

           public void keyReleased(final KeyEvent e) {
               // NO-OP
           }
       });
       // Handle mouse clicks
       tree.addMouseListener(new MouseAdapter() {
           @Override
            public void mouseDoubleClick(MouseEvent e) {
               gotoSelectedElement();
            }
       });
       
       // Handle widget selection events
       tree.addSelectionListener(new SelectionListener() {
           public void widgetSelected(final SelectionEvent e) {
               // NO-OP
           }

           public void widgetDefaultSelected(final SelectionEvent e) {
               gotoSelectedElement();
           }
       });
   }
    
    /**
     * @return
     */
    private Object getSelectedElement() {
        if (treeViewer == null) {
            return null;
        }
        return ((IStructuredSelection) treeViewer.getSelection())
                .getFirstElement();
    }

    @Override
    protected Control createTitleControl(final Composite parent) {
        // Applies only to dialog title - not body. See createDialogArea
        // Create the text widget
        createUIWidgetFilterText(parent);
        // Add listeners to the text widget
        createUIListenersFilterText();
        // Return the text widget
        return filterText;
    }

    /**
     * @param parent
     * @return
     */
    private void createUIWidgetFilterText(final Composite parent) {
        // Create the widget
        filterText = new Text(parent, SWT.NONE);
        // Set the font
        final GC gc = new GC(parent);
        gc.setFont(parent.getFont());
        final FontMetrics fontMetrics = gc.getFontMetrics();
        gc.dispose();
        // Create the layout
        final GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.heightHint = Dialog.convertHeightInCharsToPixels(fontMetrics, 1);
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.CENTER;
        filterText.setLayoutData(data);
    }

    /**
     *
     */
    private void createUIListenersFilterText() {
        // Handle key events
        filterText.addKeyListener(new KeyListener() {
            public void keyPressed(final KeyEvent e) {
                if (e.keyCode == 0x0D) {
                    // Return key was pressed
                    gotoSelectedElement();
                } else if (e.keyCode == SWT.ARROW_DOWN) {
                    // Down key was pressed
                    treeViewer.getTree().setFocus();
                } else if (e.keyCode == SWT.ARROW_UP) {
                    // Up key was pressed
                    treeViewer.getTree().setFocus();
                } else if (e.character == 0x1B) {
                    // Escape key was pressed
                    close();
                }
            }

            public void keyReleased(final KeyEvent e) {
                // NO-OP
            }
        });
        // Handle text modify events
        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(final ModifyEvent e) {
                String text = ((Text) e.widget).getText();
                final int length = text.length();
                if (length > 0) {
                    // Append a '*' pattern to the end of the text value if it
                    // does not have one already
                    if (text.charAt(length - 1) != '*') {
                        text = text + '*';
                    }
                    // Prepend a '*' pattern to the beginning of the text value
                    // if it does not have one already
                    if (text.charAt(0) != '*') {
                        text = '*' + text;
                    }
                }
                // Set and update the pattern
                setMatcherString(text, true);
            }
        });
    }
    
    private void gotoSelectedElement() {
        OutlineModel selectedElement = (OutlineModel)getSelectedElement();
        editor.showRange(selectedElement.getOffset(), selectedElement.getLength());
        close();
    }

    private Pattern namePattern;

    /**
     * Sets the patterns to filter out for the receiver.
     * <p>
     * The following characters have special meaning: ? => any character * =>
     * any string
     * </p>
     * 
     * @param pattern
     *            the pattern
     * @param update
     *            <code>true</code> if the viewer should be updated
     */
    void setMatcherString(final String pattern, final boolean update) {
        if (pattern.length() == 0) {
            namePattern = null;
        } else {
            namePattern = Strings.convertGlobToPattern(pattern);
        }

        // Update the name pattern filter on the tree viewer
        fNamePatternFilter.setPattern(namePattern);

        // Update the tree viewer according to the pattern
        if (update) {
            namePatternUpdated();
        }
    }

    /**
     * The string matcher has been modified. The default implementation
     * refreshes the view and selects the first matched element
     */
    private void namePatternUpdated() {
        // Refresh the tree viewer to re-filter
        treeViewer.getControl().setRedraw(false);
        treeViewer.refresh();
        treeViewer.expandAll();
        selectFirstMatch();
        treeViewer.getControl().setRedraw(true);
    }

    /**
     * Selects the first element in the tree which matches the current filter
     * pattern.
     */
    private void selectFirstMatch() {
        final Tree tree = treeViewer.getTree();
        final Object element = findFirstMatchToPattern(tree.getItems());
        if (element != null) {
            treeViewer.setSelection(new StructuredSelection(element), true);
        } else {
            treeViewer.setSelection(StructuredSelection.EMPTY);
        }
    }

    /**
     * @param items
     * @return
     */
    private Object findFirstMatchToPattern(final TreeItem[] items) {
        // Match the string pattern against labels
        final TextProvider textProvider = (TextProvider) treeViewer.getLabelProvider();
        // Process each item in the tree
        for (int i = 0; i < items.length; i++) {
            Object element = items[i].getData();
            // Return the first element if no pattern is set
            if (namePattern == null) {
                return element;
            }
            // Return the element if it matches the pattern
            if (element != null) {
                final String label = textProvider.textOf(element);
                if (namePattern.matcher(label).matches()) {
                    return element;
                }
            }
            // Recursively check the elements children for a match
            element = findFirstMatchToPattern(items[i].getItems());
            // Return the child element match if found
            if (element != null) {
                return element;
            }
        }
        // No match found
        return null;
    }
    
}
