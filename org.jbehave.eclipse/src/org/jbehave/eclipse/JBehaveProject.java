package org.jbehave.eclipse;

import static org.jbehave.eclipse.util.Objects.o;

import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.jbehave.core.steps.StepType;
import org.jbehave.eclipse.cache.JavaScanner;
import org.jbehave.eclipse.cache.MethodCache;
import org.jbehave.eclipse.cache.MethodCache.Callback;
import org.jbehave.eclipse.cache.StepCandidateCacheListener;
import org.jbehave.eclipse.cache.StepCandidateCacheLoader;
import org.jbehave.eclipse.cache.container.Container;
import org.jbehave.eclipse.editor.step.LocalizedStepSupport;
import org.jbehave.eclipse.editor.step.MethodToStepCandidateReducer;
import org.jbehave.eclipse.editor.step.StepCandidate;
import org.jbehave.eclipse.editor.step.StepCandidateReduceListener;
import org.jbehave.eclipse.editor.step.StepLocator;
import org.jbehave.eclipse.preferences.ClassScannerPreferences;
import org.jbehave.eclipse.preferences.ProjectPreferences;
import org.jbehave.eclipse.util.LocaleUtils;
import org.jbehave.eclipse.util.New;
import org.jbehave.eclipse.util.Visitor;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fj.Effect;

public class JBehaveProject implements StepCandidateCacheListener {
    private static Logger log = LoggerFactory.getLogger(JBehaveProject.class);

    private IProject project;
    private MethodCache<StepCandidate> cache;
    private StepCandidateCacheLoader cacheLoader;
    private LocalizedStepSupport localizedStepSupport;
    private ProjectPreferences projectPreferences;
    private ClassScannerPreferences classScannerPreferences;
    //
    private CopyOnWriteArrayList<JBehaveProjectListener> listeners = New.copyOnWriteArrayList();

    private String parameterPrefix;

    public JBehaveProject(IProject project) {
        this.project = project;
	this.cacheLoader = new StepCandidateCacheLoader(this,
		Activator.getDefault().getExecutor(),
		JBehaveProject.getSystemJobAsExecutor("JBehave cache refresh task"));
        this.localizedStepSupport = new LocalizedStepSupport();
        initializeProjectPreferencesAndListener(project);
        initializeClassScannerPreferencesAndListener(project);
    }

    private MethodCache<StepCandidate> createMethodCache() {
	return new MethodCache<StepCandidate>(newCallback());
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
                requestCacheReload();
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
            public void op(IMethod method, final Container<StepCandidate> container) {
        	StepCandidateReduceListener listener = getStepCandidateReduceListener(container);
                MethodToStepCandidateReducer reducer =
            	    new MethodToStepCandidateReducer();
                
                try {
                    reducer.reduce(method, listener);
                } catch (JavaModelException e) {
                    log.error("Failed to add step candidates for method {}", method, e);
                }
            }
        };
    }

    private StepCandidateReduceListener getStepCandidateReduceListener(
	    final Container<StepCandidate> container) {
	return new StepCandidateReduceListener() {
	    
	    @Override
	    public void add(IMethod method, StepType stepType, String stepPattern,
		    Integer priority) {
		container.add(new StepCandidate(getLocalizedStepSupport(),
			parameterPrefix, method, stepType, stepPattern, priority));
	    }
	};
    };
    
    public void notifyChanges(IJavaElementDelta delta) {
        int kind = delta.getKind();
        log.debug("Notifying change within project {}: {} ({})", o(project.getName(), delta, Integer.toBinaryString(kind)));
        requestCacheReload();
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
	if (this.cache == null) {
	    this.cache = createMethodCache();
	    requestCacheReload();
	}
        log.debug("Traversing cache for project " + project.getName());
        this.cache.traverse(visitor);
    }

    private void requestCacheReload() {
	IJavaProject javaProject = (IJavaProject)JavaCore.create(project);
	this.cacheLoader.requestReload(createMethodCache(), javaProject, getMethodCacheScanInitializer());
    }
    
    private Effect<JavaScanner<?>> getMethodCacheScanInitializer() {
	return new Effect<JavaScanner<?>>() {
	    @Override
	    public void e(JavaScanner<?> scanner) {
		scanner.setFilterHash(classScannerPreferences.calculateHash());
		scanner.setPackageRootNameFilter(classScannerPreferences
			.getPackageRootMatcher());
		scanner.setPackageNameFilter(classScannerPreferences
			.getPackageMatcher());
		scanner.setClassNameFilter(classScannerPreferences
			.getClassMatcher());
	    }
	};
    }

    private static Executor getSystemJobAsExecutor(final String jobName) {
	return new Executor() {
	    
	    @Override
	    public void execute(Runnable command) {
		JBehaveProject.runRunnableAsSystemJob(command, jobName);
	    }
	};
    }

    private static void runRunnableAsSystemJob(final Runnable runnable, final String jobName) {
        Job job = new Job(jobName) {
            @Override
            protected IStatus run(IProgressMonitor monitor) {
        	runnable.run();

                return Status.OK_STATUS;
            }
        };
        
        job.setUser(false);
        job.setSystem(true);
        job.schedule();
    }

    /** {@inheritDoc} */
    @Override
    public void cacheLoaded(MethodCache<StepCandidate> cache) {
	this.cache = cache;

	for (JBehaveProjectListener listener : listeners) {
	    try {
		listener.stepsUpdated();
	    } catch (Exception e) {
		log.error("Error during step invalidation notification: {}",
			listener, e);
	    }
	}
    }
}
