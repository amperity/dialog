package org.slf4j.impl;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;
import dialog.logger.DialogFactory;
import org.slf4j.ILoggerFactory;


/**
 * Static binding class for SLF4J integration.
 */
public final class StaticLoggerBinder {

    /**
     * Declare the version of the SLF4J API this implementation is compiled against.
     *
     * This should be kept in sync with the version declared in project.clj
     * TODO: add a CI test checking this invariant.
     */
    public static final String REQUESTED_API_VERSION = "1.7.30";


    /**
     * Initialized logger factory.
     */
    private final DialogFactory factory;


    /**
     * Private constructor.
     *
     * @param factory  constructed logger factory
     */
    private StaticLoggerBinder(DialogFactory factory) {
        this.factory = factory;
    }


    /**
     * Wrapper class to efficiently ensure the singleton is only created once.
     */
    private static class Singleton {

        private static final StaticLoggerBinder instance;

        static {
            IFn resolve = RT.var("clojure.core", "requiring-resolve");

            // Load configuration.
            Symbol initConfigName = Symbol.intern("dialog.config", "load-config");
            IFn initConfig = (IFn)resolve.invoke(initConfigName);
            Object config = initConfig.invoke();

            // Resolve enabled fn.
            Symbol isEnabledName = Symbol.intern("dialog.logger", "enabled?");
            IFn isEnabled = (IFn)resolve.invoke(isEnabledName);

            // Resolve log event fn.
            Symbol logEventName = Symbol.intern("dialog.logger", "log-event");
            IFn logEvent = (IFn)resolve.invoke(logEventName);

            // Construct singleton
            DialogFactory factory = new DialogFactory(config, isEnabled, logEvent);
            instance = new StaticLoggerBinder(factory);
        }

    }


    /**
     * Return the singleton of this class.
     *
     * @return singleton logger binder
     */
    public static final StaticLoggerBinder getSingleton() {
        return Singleton.instance;
    }


    /**
     * Get the name of the logger factory that will be instantiated.
     *
     * @return fully-qualified class name
     */
    public String getLoggerFactoryClassStr() {
        return DialogFactory.class.getName();
    }


    /**
     * Return a configured logger factory implementation.
     *
     * @return logger factory
     */
    public ILoggerFactory getLoggerFactory() {
        return factory;
    }

}
