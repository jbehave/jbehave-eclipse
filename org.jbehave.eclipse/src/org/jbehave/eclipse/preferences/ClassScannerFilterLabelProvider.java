package org.jbehave.eclipse.preferences;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.viewers.DecorationOverlayIcon;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.jbehave.eclipse.Activator;
import org.jbehave.eclipse.ImageIds;

public class ClassScannerFilterLabelProvider extends LabelProvider {

    @Override
    public Image getImage(Object element) {
        ClassScannerFilterEntry filter = (ClassScannerFilterEntry) element;
        String baseKey = getBaseImageKey(filter);

        if (filter.isExclude()) {
            String decoratedImageId = baseKey + "-excluded";
            ImageRegistry imageRegistry = Activator.getDefault().getImageRegistry();
            Image image = imageRegistry.get(decoratedImageId);
            
            // no decorated image yet, let's build it
            if (image == null) {
                Image baseImage = JavaUI.getSharedImages().getImage(baseKey);
                ImageDescriptor overlay = getExcludeOverlay();

                // Otherwise create a new image and store it
                DecorationOverlayIcon decoratedImage = new DecorationOverlayIcon(baseImage, overlay,
                        IDecoration.BOTTOM_LEFT);
                imageRegistry.put(decoratedImageId, decoratedImage);
                image = imageRegistry.get(decoratedImageId);
            }
            return image;
        } else {
            return JavaUI.getSharedImages().getImage(baseKey);
        }
    }

    private String getBaseImageKey(ClassScannerFilterEntry filter) {
        String baseKey = ISharedImages.IMG_OBJS_CLASS;
        switch (filter.getApplyOn()) {
            case PackageRoot:
                return ISharedImages.IMG_OBJS_LIBRARY;
            case Package:
                baseKey = ISharedImages.IMG_OBJS_PACKAGE;
                break;
            case Class:
            default:
                baseKey = ISharedImages.IMG_OBJS_CLASS;
        }
        return baseKey;
    }

    private ImageDescriptor getExcludeOverlay() {
//        return PlatformUI.getWorkbench().getSharedImages()
//                .getImageDescriptor(org.eclipse.ui.ISharedImages.IMG_DEC_FIELD_ERROR);
        return Activator.getDefault().getImageRegistry().getDescriptor(ImageIds.FORBIDDEN_OVERLAY);
    }

    @Override
    public String getText(Object element) {
        ClassScannerFilterEntry filter = (ClassScannerFilterEntry) element;
        return filter.getPatterns();
    }

}
