package org.jbehave.eclipse;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class JBehaveProjectRegistry {
    
    static boolean AwareOfJDTChange = true;
    
    private static JBehaveProjectRegistry singleton = new JBehaveProjectRegistry();
    public static JBehaveProjectRegistry get() {
        singleton.registerListenerIfRequired();
        return singleton;
    }
    
    private ConcurrentHashMap<IProject, JBehaveProject> projectCache = new ConcurrentHashMap<IProject, JBehaveProject>();
    private AtomicBoolean listening = new AtomicBoolean();
    
    private void registerListenerIfRequired() {
        if(!AwareOfJDTChange)
            return;
        
        if(listening.compareAndSet(false, true)) {
            JavaCore.addElementChangedListener(new IElementChangedListener() {
                @Override
                public void elementChanged(ElementChangedEvent event) {
                    notifyChanges(event.getDelta());
                }
            });
        }
    }
    
    private void notifyChanges(IJavaElementDelta delta) {
        IProject project = extractProject(delta);
        if(project!=null)
            notifyProjectChanges(project, delta); 
    }
    
    private static IProject extractProject(IJavaElementDelta delta) {
        IJavaElement element = delta.getElement();
        if(element==null)
            return null;
        IJavaProject javaProject = element.getJavaProject();
        if(javaProject==null)
            return null;
        return javaProject.getProject();
    }
    
    protected void notifyProjectChanges(IProject project, IJavaElementDelta delta) {
        // don't call getOrCreateProject
        JBehaveProject jproject = projectCache.get(project);
        if(jproject!=null) {
            jproject.notifyChanges(delta);
        }
    }
    
    /**
     * 
     * @param project
     * @return
     * @see #getOrCreateProject(IProject)
     */
    public JBehaveProject getProject(IProject project) {
        JBehaveProject cache = projectCache.get(project);
        return cache;
    }
    
    public JBehaveProject getOrCreateProject(IProject project) {
        JBehaveProject cache = projectCache.get(project);
        if(cache==null) {
            JBehaveProject newCache = new JBehaveProject (project);
            cache = projectCache.putIfAbsent(project, newCache);
            if(cache==null)
                cache = newCache;
        }
        return cache;
    }
    
}
