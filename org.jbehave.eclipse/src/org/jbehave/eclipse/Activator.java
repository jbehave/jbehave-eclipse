package org.jbehave.eclipse;

import java.io.IOException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.ContextTypeRegistry;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.editors.text.templates.ContributionContextTypeRegistry;
import org.eclipse.ui.editors.text.templates.ContributionTemplateStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.jbehave.eclipse.console.JBehaveConsoleAppender;
import org.jbehave.eclipse.editor.story.completion.StoryContextType;
import org.jbehave.eclipse.preferences.LoggerEntry;
import org.jbehave.eclipse.preferences.LoggerPreferences;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.BackingStoreException;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import fj.data.List;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.jbehave.eclipse"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;

	// The Contribution registry and template store
    private ContributionContextTypeRegistry registry;
    private ContributionTemplateStore templateStore;
    private String version;

    // Key to store custom templates
    private static final String TEMPLATES_KEY = "org.jbehave.eclipse.templates"; //$NON-NLS-1$
	
	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		Bundle bundle = context.getBundle();
		version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
		plugin = this;
	}
	
    public void initLogger () {
	    String logFile = getStateLocation().append("plugin.log").toOSString();
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		// rolling file
		PatternLayoutEncoder encoder = createLoggerPattern(loggerContext, "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n");
	    RollingFileAppender<ILoggingEvent> rollingFileAppender = rollingFileLog(logFile, loggerContext, encoder);	    

	    // console
	    ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<ILoggingEvent>();
	    consoleAppender.setEncoder(encoder);
	    consoleAppender.start();

	    // jbehave console
	    PatternLayoutEncoder jbehaveEncoder = createLoggerPattern(loggerContext, "%d{HH:mm:ss.SSS} %-5level %logger{20} - %msg%n");
        JBehaveConsoleAppender jbehaveConsoleAppender = new JBehaveConsoleAppender();
	    jbehaveConsoleAppender.setEncoder(jbehaveEncoder);
	    jbehaveConsoleAppender.start();

	    // attach the appenders to the root logger
	    Logger logbackLogger = loggerContext.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        logbackLogger.detachAndStopAllAppenders();
        logbackLogger.setAdditive(true);
	    logbackLogger.addAppender(consoleAppender);
	    logbackLogger.addAppender(jbehaveConsoleAppender);
	    logbackLogger.addAppender(rollingFileAppender);
	    resetLoggerLevels();
	    
	    // TODO: investigate why it's not fired on preference flush...
	    getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                resetLoggerLevels();
            }
        });
	    
	    logInfo("Log file at " + logFile);
	}

    protected PatternLayoutEncoder createLoggerPattern(LoggerContext loggerContext, String pattern) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        encoder.setContext(loggerContext);
        encoder.setPattern(pattern);
        encoder.start();
        return encoder;
    }

    public void resetLoggerLevels() {
        logInfo("Resetting log levels");

        try {
            LoggerPreferences prefs = new LoggerPreferences();
            prefs.load();
            
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            for(Logger logger : loggerContext.getLoggerList()) {
                // The level of the root logger cannot be set to null
                if(!org.slf4j.Logger.ROOT_LOGGER_NAME.equals(logger.getName()))
                    logger.setLevel(null);
            }
            List<LoggerEntry> loggerEntries = prefs.getLoggerEntries();
            logInfo("About to define #" + loggerEntries.length() + "loggers");
            for(LoggerEntry entry : loggerEntries) {
                logInfo("Defining logger <" + entry.getLoggerName() + "> at level <" + entry.getLevel() + ">");
                loggerContext.getLogger(entry.getLoggerName()).setLevel(entry.getLevel());
            }
        } catch (BackingStoreException e) {
            logError("Failed to define logger levels", e);
        }
    }

    protected RollingFileAppender<ILoggingEvent> rollingFileLog(String logFile, LoggerContext loggerContext,
            PatternLayoutEncoder encoder) {
        RollingFileAppender<ILoggingEvent> appender = new RollingFileAppender<ILoggingEvent>();
	    appender.setContext(loggerContext);
        appender.setFile(logFile);
	    FixedWindowRollingPolicy policy = new FixedWindowRollingPolicy();
	    policy.setContext(loggerContext);
	    // rolling policies need to know their parent
	    // it's one of the rare cases, where a sub-component knows about its parent
	    policy.setParent(appender);
	    policy.setFileNamePattern("plugin.%i.log.zip");
	    policy.start();

	    SizeBasedTriggeringPolicy<ILoggingEvent> triggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
	    triggeringPolicy.setMaxFileSize("5MB");
	    triggeringPolicy.start();

	    appender.setEncoder(encoder);
	    appender.setRollingPolicy(policy);
	    appender.setTriggeringPolicy(triggeringPolicy);

	    appender.start();
        return appender;
    }

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
	
	public KeywordImages getKeywordImages() {
	    return new KeywordImages(getImageRegistry());
	}
	
	@Override
	protected void initializeImageRegistry(ImageRegistry registry) {
	    super.initializeImageRegistry(registry);
	    registry.put(ImageIds.STEP_GIVEN, getImageDescriptor("icons/bdd-g-blue.png"));
        registry.put(ImageIds.STEP_WHEN,  getImageDescriptor("icons/bdd-w-orange.png"));
        registry.put(ImageIds.STEP_THEN,  getImageDescriptor("icons/bdd-t-green.png"));
        registry.put(ImageIds.STEP_AND,   getImageDescriptor("icons/bdd-a-lighterblue.png"));
        //
        registry.put(ImageIds.META,       getImageDescriptor("icons/bdd-m-pink.png"));
        registry.put(ImageIds.STORY,      getImageDescriptor("icons/bdd-s-darkpink.png"));
        registry.put(ImageIds.IGNORABLE,   getImageDescriptor("icons/bdd-i-cacadoie.png"));
        //
        registry.put(ImageIds.NARRATIVE,  getImageDescriptor("icons/bdd-n-darkred.png"));
        registry.put(ImageIds.SCENARIO,  getImageDescriptor("icons/bdd-s-darkpink.png"));
        registry.put(ImageIds.EXAMPLE_TABLE,  getImageDescriptor("icons/bdd-e-turquoise.png"));
        //
        registry.put(ImageIds.FORBIDDEN_OVERLAY, getImageDescriptor("icons/error_ovr16.gif"));
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
	    return super.getPreferenceStore();
	}
	
	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public TemplateStore getTemplateStore() {
        // this is to avoid recursive call when fContextTypeRegistry is null
        getContextTypeRegistry();
        if (templateStore == null) {
            templateStore = new ContributionTemplateStore(
                    getContextTypeRegistry(), getPreferenceStore(),
                    TEMPLATES_KEY);
            try {
                templateStore.load();
            } catch (final IOException e) {
                getLog().log(
                        new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, "", e)); //$NON-NLS-1$
            }
        }
        return templateStore;
    }
	
	public ContextTypeRegistry getContextTypeRegistry() {
        if (registry == null) {
            // create an configure the contexts available in the template editor
            registry = new ContributionContextTypeRegistry();
            registry.addContextType(StoryContextType.STORY_CONTEXT_TYPE_ID);
        }
        return registry;
    }
	
	private AtomicInteger idGen = new AtomicInteger();
	private ExecutorService executor = Executors.newFixedThreadPool(4, new ThreadFactory() {
	    private ThreadGroup group = new ThreadGroup("AsyncExecutor") {
	        @Override
	        public void uncaughtException(Thread t, Throwable e) {
	            logError("Uncaught exception in asynchronous executor", e);
	            super.uncaughtException(t, e);
	        }
	    };
	    public Thread newThread(Runnable r) {
	        Thread thr = new Thread (group, r, "JBehaveWorker#" + idGen.incrementAndGet());
	        return thr;
	    }
	});
    public Executor getExecutor() {
        return this.executor;
    }
	
    public static void logInfo(String message) {
        getDefault().getLog().log(new Status(Status.INFO, PLUGIN_ID, message));
    }

    public static void logError(String message, Throwable e) {
        getDefault().getLog().log(new Status(Status.ERROR, PLUGIN_ID, message, e));        
    }

    public String getVersion() {
        return version;
    }
}
