package org.slf4j.impl;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;

import dialog.logger.DialogFactory;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;
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

            // Initialize configuration.
            Symbol initConfigName = Symbol.intern("dialog.logger", "init-config!");
            IFn initConfig = (IFn)resolve.invoke(initConfigName);
            initConfig.invoke();

            // Resolve enabled fn.
            Symbol isEnabledName = Symbol.intern("dialog.logger", "enabled?");
            IFn isEnabled = (IFn)resolve.invoke(isEnabledName);

            // Resolve log event fn.
            Symbol logMessageName = Symbol.intern("dialog.logger", "log-message");
            IFn logMessage = (IFn)resolve.invoke(logMessageName);

            // Construct singleton
            DialogFactory factory = new DialogFactory(isEnabled, logMessage);
            instance = new StaticLoggerBinder(factory);

            // Configure JUL bridge
            LogManager.getLogManager().reset();
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            Logger.getGlobal().setLevel(Level.FINEST);
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
