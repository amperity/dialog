package dialog.logger;

import clojure.lang.IFn;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.ILoggerFactory;


/**
 * Logger factory for integrating with SLF4J.
 */
public final class DialogFactory implements ILoggerFactory {

    /**
     * Logger cache.
     */
    private final ConcurrentHashMap<String,DialogLogger> cache;


    /**
     * Log level lookup function.
     */
    private final IFn getLevelFn;


    /**
     * Log event entry function.
     */
    private final IFn logMessageFn;


    /**
     * Construct a new logger factory.
     *
     * @param getLevelFn    function to get the level for a logger
     * @param logMessageFn  function to log an event
     */
    public DialogFactory(IFn getLevelFn, IFn logMessageFn) {
        this.cache = new ConcurrentHashMap<>(64);
        this.getLevelFn = getLevelFn;
        this.logMessageFn = logMessageFn;
    }


    @Override
    public DialogLogger getLogger(String name) {
        // Check for existing cached logger.
        DialogLogger extant = cache.get(name);
        if (extant != null) {
            return extant;
        }

        // NOTE: this is subject to race conditions since we don't want to pay
        // the cost of locking; however in the normal case most classes obtain
        // loggers statically, so this is unlikely to actually produce
        // duplicates. Even if it does, usage is still safe and the cost is
        // minimal.
        DialogLogger logger = new DialogLogger(name, getLevelFn, logMessageFn);
        cache.put(name, logger);

        return logger;
    }

}
