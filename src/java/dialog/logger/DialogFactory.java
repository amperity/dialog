package dialog.logger;

import clojure.lang.IFn;
import org.slf4j.ILoggerFactory;


/**
 * Logger factory for integrating with SLF4J.
 */
public final class DialogFactory implements ILoggerFactory {

    // TODO: should config be a class field?


    /**
     * Logging configuration map.
     */
    private final Object config;


    /**
     * Log level enabled check.
     */
    private final IFn isEnabledFn;


    /**
     * Log event entry.
     */
    private final IFn logMessageFn;


    /**
     * Construct a new logger factory.
     *
     * @param config        initialized configuration
     * @param isEnabledFn   function to check whether the logger is enabled
     * @param logMessageFn  function to log an event
     */
    public DialogFactory(Object config, IFn isEnabledFn, IFn logMessageFn) {
        this.config = config;
        this.isEnabledFn = isEnabledFn;
        this.logMessageFn = logMessageFn;
    }


    @Override
    public DialogLogger getLogger(String name) {
        // TODO: cache loggers?
        return new DialogLogger(name, config, isEnabledFn, logMessageFn);
    }

}
