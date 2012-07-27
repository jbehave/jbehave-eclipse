/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.jbehave.eclipse.editor.story;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.jbehave.eclipse.editor.text.TextProvider;

/**
 * PatternViewFilter
 * 
 * <strong>Important</strong>: The filter rely on {@link TextProvider} to
 * retrieve the text from the element. The default implementation considers that
 * the viewer's {@link IBaseLabelProvider} implements {@link TextProvider},
 * override {@link #getTextProvider(TreeViewer)} to change this behavior.
 */
public class PatternViewFilter extends ViewerFilter {

    private Pattern pattern;

    /**
     * @see #getTextProvider(TreeViewer)
     */
    public PatternViewFilter() {
        pattern = null;
    }

    @Override
    public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
        // Element passes the filter if the pattern is undefined or the
        // viewer is not a tree viewer
        if (pattern == null || !(viewer instanceof TreeViewer)) {
            return true;
        }

        TreeViewer treeViewer = (TreeViewer) viewer;

        // Match the pattern against the label of the given element
        TextProvider textProvider = getTextProvider(treeViewer);
        String text = textProvider.textOf(element);

        // Element passes the filter if it matches the pattern
        if (text != null) {
            boolean matches = pattern.matcher(text).matches();
            if (matches) {
                return true;
            }
        }
        // Determine whether the element has children that pass the filter
        return hasUnfilteredChild(treeViewer, element);
    }

    /**
     * The default implementation considers
     * that the viewer's {@link IBaseLabelProvider} implements
     * {@link TextProvider}.
     * 
     * Override this method to change the default behavior.
     * 
     * @param treeViewer
     * @return
     */
    protected TextProvider getTextProvider(TreeViewer treeViewer) {
        return (TextProvider) treeViewer.getLabelProvider();
    }

    private boolean hasUnfilteredChild(final TreeViewer viewer, final Object element) {
        // No point calling hasChildren() because the operation is the same cost
        // as getting the children
        // If the element has a child that passes the filter, then we want to
        // keep the parent around - even if it does not pass the filter itself
        final Object[] children = ((ITreeContentProvider) viewer.getContentProvider()).getChildren(element);
        for (int i = 0; i < children.length; i++) {
            if (select(viewer, element, children[i])) {
                return true;
            }
        }
        // Element does not pass the filter
        return false;
    }

    /**
     * @param pattern
     *            used to filter the viewer's element
     */
    public void setPattern(final Pattern pattern) {
        this.pattern = pattern;
    }

}
