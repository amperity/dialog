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
    private final IFn isEnabled;


    /**
     * Log event entry.
     */
    private final IFn logEvent;


    /**
     * Construct a new logger factory.
     *
     * @param config     initialized configuration
     * @param isEnabled  function to check whether the logger is enabled
     * @param logEvent   function to log an event
     */
    public DialogFactory(Object config, IFn isEnabled, IFn logEvent) {
        this.config = config;
        this.isEnabled = isEnabled;
        this.logEvent = logEvent;
    }


    @Override
    public DialogLogger getLogger(String name) {
        // TODO: cache loggers?
        return new DialogLogger(name, config, isEnabled, logEvent);
    }

}
