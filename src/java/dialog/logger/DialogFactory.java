package dialog.logger;

import org.slf4j.ILoggerFactory;


/**
 * Logger factory for integrating with SLF4J.
 */
public final class DialogFactory implements ILoggerFactory {

    /**
     * Construct a new logger factory.
     */
    protected DialogFactory() {
        // TODO: configuration...
    }


    @Override
    public DialogLogger getLogger(String name) {
        // TODO: implement
        return null;
    }

}
