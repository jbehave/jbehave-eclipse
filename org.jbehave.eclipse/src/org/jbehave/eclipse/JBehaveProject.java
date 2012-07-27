package org.jbehave.eclipse;

import static org.jbehave.eclipse.util.Objects.o;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.steps.PatternVariantBuilder;
import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.cache.JavaScanner;
import org.jbehave.eclipse.cache.MethodCache;
import org.jbehave.eclipse.cache.MethodCache.Callback;
import org.jbehave.eclipse.cache.container.Container;
import org.jbehave.eclipse.cache.container.Containers;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.step.StepLocator;
import org.jbehave.eclipse.preferences.ClassScannerPreferences;
import org.jbehave.eclipse.preferences.ProjectPreferences;
import org.jbehave.eclipse.util.LocaleUtils;
import org.jbehave.eclipse.util.New;
import org.jbehave.eclipse.util.ProcessGroup;
import org.jbehave.eclipse.util.StringDecorator;
import org.jbehave.eclipse.util.Visitor;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Effect;

public class JBehaveProject {
    private static Logger log = LoggerFactory.getLogger(JBehaveProject.class);

    private IProject project;
    private MethodCache<StepCandidate> cache;
    private LocalizedStepSupport localizedStepSupport;
    private ProjectPreferences projectPreferences;
    private ClassScannerPreferences classScannerPreferences;
    //
    private ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private AtomicInteger comod = new AtomicInteger();
    private volatile int rebuildTick = -1;
    //
    private CopyOnWriteArrayList<JBehaveProjectListener> listeners = New.copyOnWriteArrayList();

    private String parameterPrefix;

    public JBehaveProject(IProject project) {
        this.project = project;
        this.cache = new MethodCache<StepCandidate>(newCallback());
        this.localizedStepSupport = new LocalizedStepSupport();
        initializeProjectPreferencesAndListener(project);
        initializeClassScannerPreferencesAndListener(project);
    }

    protected void initializeClassScannerPreferencesAndListener(IProject project) {
        this.classScannerPreferences = new ClassScannerPreferences(project);
        this.classScannerPreferences.addListener(new IPreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent changeEvent) {
                log.info("Class scanner preference changed [{}]: <{}> -> <{}>",
                        o(changeEvent.getKey(), changeEvent.getOldValue(), changeEvent.getNewValue()));
                reloadScannerPreferences();
            }
        });
        this.reloadScannerPreferences();
    }

    protected void initializeProjectPreferencesAndListener(IProject project) {
        this.projectPreferences = new ProjectPreferences(project);
        this.projectPreferences.addListener(new IPreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent changeEvent) {
                log.info("Project preference changed [{}]: <{}> -> <{}>",
                        o(changeEvent.getKey(), changeEvent.getOldValue(), changeEvent.getNewValue()));
                reloadProjectPreferences();
            }
        });
        this.reloadProjectPreferences();
    }

    protected void reloadScannerPreferences() {
        try {
            classScannerPreferences.load();
        } catch (BackingStoreException e) {
            log.error("Failed to load scanner preferences", e);
        }
    }

    private void reloadProjectPreferences() {
        try {
            projectPreferences.load();
        } catch (BackingStoreException e) {
            log.error("Failed to load project preferences", e);
        }
        Locale storyLocale = LocaleUtils.createLocaleFromCode(projectPreferences.getStoryLanguage(), Locale.ENGLISH);
        localizedStepSupport.setStoryLocale(storyLocale);
        parameterPrefix = projectPreferences.getParameterPrefix();
        
        log.info("Reloading project preferences, story locale: {}, parameter prefix: {}", storyLocale, parameterPrefix);
        invalidateCache();
    }
    
    public void addListener(JBehaveProjectListener listener) {
        listeners.add(listener);
    }

    public void removeListener(JBehaveProjectListener listener) {
        if(listener==null)
            return;
        listeners.remove(listener);
    }
    
    public LocalizedStepSupport getLocalizedStepSupport() {
        return localizedStepSupport;
    }

    public Locale getLocale() {
        return getLocalizedStepSupport().getLocale();
    }

    private Callback<IMethod, Container<StepCandidate>> newCallback() {
        return new Callback<IMethod, Container<StepCandidate>>() {
            public void op(IMethod method, Container<StepCandidate> container) {
                try {
                    addStepCandidates(method, container);
                } catch (JavaModelException e) {
                    log.error("Failed to add step candidates for method {}", method, e);
                }
            };
        };
    }

    public void notifyChanges(IJavaElementDelta delta) {
        int kind = delta.getKind();
        log.debug("Notifying change within project {}: {} ({})", o(project.getName(), delta, Integer.toBinaryString(kind)));
        invalidateCache();
    }

    public IProject getProject() {
        return project;
    }
    
    public ProjectPreferences getProjectPreferences() {
		return projectPreferences;
	}

	public StepLocator getStepLocator() {
        return new StepLocator(this);
    }

    public void traverseSteps(Visitor<StepCandidate, ?> visitor) throws JavaModelException {
        boolean rAcquired = true;
        rwLock.readLock().lock();
        try {
            int mod = comod.get();
            if (rebuildTick != mod) {
                // promote lock
                rwLock.readLock().unlock(); // must unlock first to obtain writelock
                rAcquired = false;
                rwLock.writeLock().lock();
                try {
                    mod = comod.get();
                    if (rebuildTick != mod) {
                        rebuildTick = mod;
                        rebuild();
                    }
                    // Downgrade by acquiring read lock before releasing write lock
                    rwLock.readLock().lock();
                    rAcquired = true;
                } finally {
                    rwLock.writeLock().unlock(); // Unlock write, still hold read
                }
            }

            log.debug("Traversing cache for project " + project.getName());
            cache.traverse(visitor);
        } finally {
            if (rAcquired)
                rwLock.readLock().unlock();
        }
    }

    protected void rebuild() {
        log.info("Rebuilding cache for project " + project.getName());

        ProcessGroup<Void> processGroup = Activator.getDefault().newProcessGroup();
        try {
            cache.rebuild(project, new Effect<JavaScanner<?>>() {
                @Override
                public void e(JavaScanner<?> scanner) {
                    scanner.setFilterHash(classScannerPreferences.calculateHash());
                    scanner.setPackageRootNameFilter(classScannerPreferences.getPackageRootMatcher());
                    scanner.setPackageNameFilter(classScannerPreferences.getPackageMatcher());
                    scanner.setClassNameFilter(classScannerPreferences.getClassMatcher());
                }
            }, processGroup);
        } catch (JavaModelException e) {
            log.error("Error during cache rebuild", e);
            invalidateCache();
        }

        try {
            processGroup.awaitTermination();
        } catch (InterruptedException e) {
            log.warn("Cache rebuild interrupted");
            invalidateCache();
        }
    }

    protected void invalidateCache() {
        // invalidate cache
        comod.incrementAndGet();
        Job job = new Job("Cache invalidated") {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                for(JBehaveProjectListener listener : listeners) {
                    try {
                        listener.stepsInvalidated();
                    } catch (Exception e) {
                        log.error("Error during step invalidation notification: {}", listener, e);
                    }
                }
                return Status.OK_STATUS;
            }
        };
        job.setUser(false);
        job.schedule();

    }

    private void addStepCandidates(IMethod method, Container<StepCandidate> container) throws JavaModelException {
        String parameterPrefix = this.parameterPrefix;
        StepType stepType = null;
        for (IAnnotation annotation : method.getAnnotations()) {
            String elementName = annotation.getElementName();
            IMemberValuePair[] annotationAttributes = annotation.getMemberValuePairs();
            Integer priority = Integer.valueOf(0);

            List<String> patterns = new ArrayList<String>();
            if (StringDecorator.decorate(elementName).endsWithOneOf("Given", "When", "Then")) {
                // TODO check import declaration matches org.jbehave...
                stepType = StepType.valueOf(elementName.toUpperCase());
                String stepPattern = getValue(annotationAttributes, "value");
                priority = getValue(annotationAttributes, "priority");
                PatternVariantBuilder b = new PatternVariantBuilder(stepPattern);
                for (String variant : b.allVariants()) {
                    patterns.add(variant);
                }
            } else if (StringDecorator.decorate(elementName).endsWithOneOf("Aliases")) {
                // TODO check import declaration matches org.jbehave...
                Object aliases = getValue(annotationAttributes, "values");
                if (aliases instanceof Object[]) {
                    for (Object o : (Object[]) aliases) {
                        if (o instanceof String) {
                            PatternVariantBuilder b = new PatternVariantBuilder((String) o);
                            for (String variant : b.allVariants()) {
                                patterns.add(variant);
                            }
                        }
                    }
                    if (!patterns.isEmpty() && stepType == null)
                        stepType = StepType.GIVEN;
                }
            } else if (StringDecorator.decorate(elementName).endsWithOneOf("Alias")) {
                // TODO check import declaration matches org.jbehave...
                String stepPattern = getValue(annotationAttributes, "value");
                PatternVariantBuilder b = new PatternVariantBuilder(stepPattern);
                for (String variant : b.allVariants()) {
                    patterns.add(variant);
                }

                if (!patterns.isEmpty() && stepType == null)
                    stepType = StepType.GIVEN;
            }

            if (!patterns.isEmpty()) {
                log.debug("Analysing method: " + Containers.pathOf(method) + " found: " + patterns);
                for (String stepPattern : patterns) {
                    if (stepPattern == null){
                        continue;
                    }
                    container.add(new StepCandidate(//
                            getLocalizedStepSupport(),//
                            parameterPrefix,//
                            method, //
                            annotation, //
                            stepType, //
                            stepPattern, //
                            priority));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(IMemberValuePair[] memberValuePairs, String key) {
        for (IMemberValuePair kv : memberValuePairs) {
            if (kv.getMemberName().equalsIgnoreCase(key))
                return (T) kv.getValue();
        }
        return null;
    }

}
