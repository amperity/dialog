package dialog.logger;

import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Symbol;

import dialog.logger.DialogFactory;

import org.slf4j.ILoggerFactory;
import org.slf4j.IMarkerFactory;
import org.slf4j.helpers.BasicMarkerFactory;
import org.slf4j.helpers.NOPMDCAdapter;
import org.slf4j.spi.MDCAdapter;
import org.slf4j.spi.SLF4JServiceProvider;

public class DialogServiceProvider implements SLF4JServiceProvider {

    /**
     * Declare the version of the SLF4J API this implementation is compiled against.
     * The value of this field is modified with each major release.
     */
    // to avoid constant folding by the compiler, this field must *not* be final
    public static String REQUESTED_API_VERSION = "2.0.99"; // !final

    private ILoggerFactory loggerFactory;
    private IMarkerFactory markerFactory;
    private MDCAdapter mdcAdapter;

    public ILoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    @Override
    public IMarkerFactory getMarkerFactory() {
        return markerFactory;
    }

    @Override
    public MDCAdapter getMDCAdapter() {
        return mdcAdapter;
    }

    @Override
    public String getRequestedApiVersion() {
        return REQUESTED_API_VERSION;
    }

    @Override
    public void initialize() {
        IFn resolve = RT.var("clojure.core", "requiring-resolve");
        // Initialize configuration.
        Symbol initConfigName = Symbol.intern("dialog.logger", "initialize!");
        IFn initConfig = (IFn)resolve.invoke(initConfigName);
        initConfig.invoke();
        // Resolve level fn.
        Symbol getLevelName = Symbol.intern("dialog.logger", "get-level");
        IFn getLevel = (IFn)resolve.invoke(getLevelName);
        // Resolve logging fn.
        Symbol logMessageName = Symbol.intern("dialog.logger", "-log-slf4j");
        IFn logMessage = (IFn)resolve.invoke(logMessageName);

        loggerFactory = new DialogFactory(getLevel, logMessage);
        markerFactory = new BasicMarkerFactory();
        mdcAdapter = new NOPMDCAdapter();
    }

}
