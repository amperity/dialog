package org.slf4j.impl;

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
     * Wrapper class to efficiently ensure the singleton is only created once.
     */
    private static class Singleton {

        private static final StaticLoggerBinder instance;

        static {
            // TODO: resolve and initialize clojure references
            instance = new StaticLoggerBinder();
        }

    }


    /**
     * Private constructor.
     */
    private StaticLoggerBinder() {
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
        // TODO: initialize factory with Clojure references
        return new DialogFactory();
    }

}
