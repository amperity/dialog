package dialog.logger;

import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;

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
     * Log level enabled check.
     */
    private final IFn isEnabledFn;


    /**
     * Log event entry.
     */
    private final IFn logMessageFn;


    /**
     * Construct a new logger.
     *
     * @param name          logger name, typically the full class name or namespace
     * @param isEnabledFn   function to check whether the logger is enabled
     * @param logMessageFn  function to log an event
     */
    protected DialogLogger(String name, IFn isEnabledFn, IFn logMessageFn) {
        this.name = name;
        this.isEnabledFn = isEnabledFn;
        this.logMessageFn = logMessageFn;
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
        IFn f = isEnabledFn;

        // DEBUG: uncomment this for development only so that code reloading works
        IFn resolve = RT.var("clojure.core", "requiring-resolve");
        Symbol isEnabledName = Symbol.intern("dialog.logger", "enabled?");
        f = (IFn)resolve.invoke(isEnabledName);

        return (Boolean)f.invoke(name, level);
    }


    /**
     * Core method which passes logged messages into the Clojure code.
     *
     * @param level  log level keyword
     * @param msg    log message
     * @param err    throwable exception associated with the message
     */
    private void logMessage(Keyword level, String msg, Throwable err) {
        IFn f = logMessageFn;

        // DEBUG: uncomment this for development only so that code reloading works
        // TODO: add a CI check for this
        IFn resolve = RT.var("clojure.core", "requiring-resolve");
        Symbol logMessageName = Symbol.intern("dialog.logger", "log-message");
        f = (IFn)resolve.invoke(logMessageName);

        f.invoke(name, level, msg, err);
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
            logMessage(TRACE, msg, null);
        }
    }


    @Override
    public void trace(String format, Object arg) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, String.format(format, arg), null);
        }
    }


    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void trace(String format, Object... args) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, String.format(format, args), null);
        }
    }


    @Override
    public void trace(String msg, Throwable err) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, msg, err);
        }
    }


    @Override
    public void trace(Marker marker, String msg) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, msg, null);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, String.format(format, arg), null);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object... args) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, String.format(format, args), null);
        }
    }


    @Override
    public void trace(Marker marker, String msg, Throwable err) {
        if (isEnabled(TRACE)) {
            logMessage(TRACE, msg, err);
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
            logMessage(DEBUG, msg, null);
        }
    }


    @Override
    public void debug(String format, Object arg) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, String.format(format, arg), null);
        }
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void debug(String format, Object... args) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, String.format(format, args), null);
        }
    }


    @Override
    public void debug(String msg, Throwable err) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, msg, err);
        }
    }


    @Override
    public void debug(Marker marker, String msg) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, msg, null);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, String.format(format, arg), null);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object... args) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, String.format(format, args), null);
        }
    }


    @Override
    public void debug(Marker marker, String msg, Throwable err) {
        if (isEnabled(DEBUG)) {
            logMessage(DEBUG, msg, err);
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
            logMessage(INFO, msg, null);
        }
    }


    @Override
    public void info(String format, Object arg) {
        if (isEnabled(INFO)) {
            logMessage(INFO, String.format(format, arg), null);
        }
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isEnabled(INFO)) {
            logMessage(INFO, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void info(String format, Object... args) {
        if (isEnabled(INFO)) {
            logMessage(INFO, String.format(format, args), null);
        }
    }


    @Override
    public void info(String msg, Throwable err) {
        if (isEnabled(INFO)) {
            logMessage(INFO, msg, err);
        }
    }


    @Override
    public void info(Marker marker, String msg) {
        if (isEnabled(INFO)) {
            logMessage(INFO, msg, null);
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        if (isEnabled(INFO)) {
            logMessage(INFO, String.format(format, arg), null);
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(INFO)) {
            logMessage(INFO, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void info(Marker marker, String format, Object... args) {
        if (isEnabled(INFO)) {
            logMessage(INFO, String.format(format, args), null);
        }
    }


    @Override
    public void info(Marker marker, String msg, Throwable err) {
        if (isEnabled(INFO)) {
            logMessage(INFO, msg, err);
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
            logMessage(WARN, msg, null);
        }
    }


    @Override
    public void warn(String format, Object arg) {
        if (isEnabled(WARN)) {
            logMessage(WARN, String.format(format, arg), null);
        }
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isEnabled(WARN)) {
            logMessage(WARN, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void warn(String format, Object... args) {
        if (isEnabled(WARN)) {
            logMessage(WARN, String.format(format, args), null);
        }
    }


    @Override
    public void warn(String msg, Throwable err) {
        if (isEnabled(WARN)) {
            logMessage(WARN, msg, err);
        }
    }


    @Override
    public void warn(Marker marker, String msg) {
        if (isEnabled(WARN)) {
            logMessage(WARN, msg, null);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (isEnabled(WARN)) {
            logMessage(WARN, String.format(format, arg), null);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(WARN)) {
            logMessage(WARN, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object... args) {
        if (isEnabled(WARN)) {
            logMessage(WARN, String.format(format, args), null);
        }
    }


    @Override
    public void warn(Marker marker, String msg, Throwable err) {
        if (isEnabled(WARN)) {
            logMessage(WARN, msg, err);
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
            logMessage(ERROR, msg, null);
        }
    }


    @Override
    public void error(String format, Object arg) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, String.format(format, arg), null);
        }
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void error(String format, Object... args) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, String.format(format, args), null);
        }
    }


    @Override
    public void error(String msg, Throwable err) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, msg, err);
        }
    }


    @Override
    public void error(Marker marker, String msg) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, msg, null);
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, String.format(format, arg), null);
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void error(Marker marker, String format, Object... args) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, String.format(format, args), null);
        }
    }


    @Override
    public void error(Marker marker, String msg, Throwable err) {
        if (isEnabled(ERROR)) {
            logMessage(ERROR, msg, err);
        }
    }

}
