package dialog.logger;

import clojure.lang.Keyword;
import org.slf4j.Logger;
import org.slf4j.Marker;


/**
 * Logger interface for integrating with SLF4J.
 */
public final class DialogLogger implements Logger {

    // Constants for log levels.
    public static final Keyword TRACE = Keyword.intern("trace");
    public static final Keyword DEBUG = Keyword.intern("debug");
    public static final Keyword INFO  = Keyword.intern("info");
    public static final Keyword WARN  = Keyword.intern("warn");
    public static final Keyword ERROR = Keyword.intern("error");


    /**
     * Name of this logger.
     */
    private final String name;


    /**
     * Construct a new logger.
     *
     * @param name  logger name, typically the full class name or namespace
     */
    protected DialogLogger(String name) {
        this.name = name;
        // TODO: more configuration...
    }


    ///// Core Methods /////

    @Override
    public String getName() {
        return name;
    }


    /**
     * Determine whether this logger is enabled for the given level.
     *
     * @param level  log level keyword
     * @return true if the logger should send messages at this level
     */
    private boolean isEnabled(Keyword level) {
        // TODO: implement
        return true;
    }


    /**
     * Core method which passes logged messages into the Clojure code.
     *
     * @param level  log level keyword
     * @param err    throwable exception associated with the message
     * @param msg    log message
     */
    private void logMessage(Keyword level, Throwable err, String msg) {
        // TODO: implement
        System.out.println(level.toString() + "\t" + msg);
    }


    ///// TRACE Methods /////

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(TRACE);
    }


    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isEnabled(TRACE);
    }


    @Override
    public void trace(String msg) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, msg);
        }
    }


    @Override
    public void trace(String format, Object arg) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, String.format(format, arg));
        }
    }


    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void trace(String format, Object... args) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, String.format(format, args));
        }
    }


    @Override
    public void trace(String msg, Throwable t) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, t, msg);
        }
    }


    @Override
    public void trace(Marker marker, String msg) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, msg);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, String.format(format, arg));
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void trace(Marker marker, String format, Object... args) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, null, String.format(format, args));
        }
    }


    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, t, msg);
        }
    }


    ///// DEBUG Methods /////

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(DEBUG);
    }


    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isEnabled(DEBUG);
    }


    @Override
    public void debug(String msg) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, msg);
        }
    }


    @Override
    public void debug(String format, Object arg) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, String.format(format, arg));
        }
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void debug(String format, Object... args) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, String.format(format, args));
        }
    }


    @Override
    public void debug(String msg, Throwable t) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, t, msg);
        }
    }


    @Override
    public void debug(Marker marker, String msg) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, msg);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, String.format(format, arg));
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void debug(Marker marker, String format, Object... args) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, null, String.format(format, args));
        }
    }


    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, t, msg);
        }
    }


    ///// INFO Methods /////

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(INFO);
    }


    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isEnabled(INFO);
    }


    @Override
    public void info(String msg) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, msg);
        }
    }


    @Override
    public void info(String format, Object arg) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, String.format(format, arg));
        }
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void info(String format, Object... args) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, String.format(format, args));
        }
    }


    @Override
    public void info(String msg, Throwable t) {
        if (isEnabled(INFO)) {
            logMessage(INFO, t, msg);
        }
    }


    @Override
    public void info(Marker marker, String msg) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, msg);
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, String.format(format, arg));
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void info(Marker marker, String format, Object... args) {
        if (isEnabled(INFO)) {
            logMessage(INFO, null, String.format(format, args));
        }
    }


    @Override
    public void info(Marker marker, String msg, Throwable t) {
        if (isEnabled(INFO)) {
            logMessage(INFO, t, msg);
        }
    }


    ///// WARN Methods /////

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(WARN);
    }


    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isEnabled(WARN);
    }


    @Override
    public void warn(String msg) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, msg);
        }
    }


    @Override
    public void warn(String format, Object arg) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, String.format(format, arg));
        }
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void warn(String format, Object... args) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, String.format(format, args));
        }
    }


    @Override
    public void warn(String msg, Throwable t) {
        if (isEnabled(WARN)) {
            logMessage(WARN, t, msg);
        }
    }


    @Override
    public void warn(Marker marker, String msg) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, msg);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, String.format(format, arg));
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void warn(Marker marker, String format, Object... args) {
        if (isEnabled(WARN)) {
            logMessage(WARN, null, String.format(format, args));
        }
    }


    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        if (isEnabled(WARN)) {
            logMessage(WARN, t, msg);
        }
    }


    ///// ERROR Methods /////

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(ERROR);
    }


    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isEnabled(ERROR);
    }


    @Override
    public void error(String msg) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, msg);
        }
    }


    @Override
    public void error(String format, Object arg) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, String.format(format, arg));
        }
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void error(String format, Object... args) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, String.format(format, args));
        }
    }


    @Override
    public void error(String msg, Throwable t) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, t, msg);
        }
    }


    @Override
    public void error(Marker marker, String msg) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, msg);
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, String.format(format, arg));
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, String.format(format, arg1, arg2));
        }
    }


    @Override
    public void error(Marker marker, String format, Object... args) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, null, String.format(format, args));
        }
    }


    @Override
    public void error(Marker marker, String msg, Throwable t) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, t, msg);
        }
    }

}
