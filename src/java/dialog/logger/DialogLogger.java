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

    /**
     * Global version counter indicating that levels may have changed.
     */
    private static int cacheVersion = 0;


    /**
     * Name of this logger.
     */
    private final String name;


    /**
     * Log level lookup funtion.
     */
    private final IFn getLevelFn;


    /**
     * Log event entry function.
     */
    private final IFn logMessageFn;


    /**
     * Cached log level threshold enum.
     */
    private Level cachedLevel;


    /**
     * Version of the cache when the threshold was last computed.
     */
    private int cachedAt;


    /**
     * Construct a new logger.
     *
     * @param name          logger name, typically the full class name or namespace
     * @param getLevelFn    function to resolve the logger level
     * @param logMessageFn  function to log an event
     */
    protected DialogLogger(String name, IFn getLevelFn, IFn logMessageFn) {
        this.name = name;
        this.getLevelFn = getLevelFn;
        this.logMessageFn = logMessageFn;
        this.cachedAt = -1;
    }


    ///// Core Methods /////

    @Override
    public String getName() {
        return name;
    }


    /**
     * Bump the class-wide cache version to force loggers to re-fetch levels.
     */
    public static void bumpCache() {
        cacheVersion += 1;
    }


    /**
     * Return the currently-configured level for this logger.
     *
     * @return Level enumeration value
     */
    public Level getLevel() {
        if (cachedLevel == null || cachedAt < cacheVersion) {
            IFn f = getLevelFn;

            // DEBUG: uncomment this for development only so that code reloading works
            //IFn resolve = RT.var("clojure.core", "requiring-resolve");
            //Symbol getLevelName = Symbol.intern("dialog.logger", "get-level");
            //f = (IFn)resolve.invoke(getLevelName);

            Keyword k = (Keyword)f.invoke(name);
            cachedLevel = Level.ofKeyword(k);
            cachedAt = cacheVersion;
        }

        return cachedLevel;
    }


    /**
     * Determine whether this logger is enabled for the given level.
     *
     * @param level  log level to test
     * @return true if the logger should send messages at this level
     */
    private boolean isEnabled(Level level) {
        return Level.isAllowed(getLevel(), level);
    }


    /**
     * Core method which passes logged messages into the Clojure code.
     *
     * @param level  log level enum
     * @param msg    log message
     * @param err    throwable exception associated with the message
     */
    private void logMessage(Level level, String msg, Throwable err) {
        IFn f = logMessageFn;

        // DEBUG: uncomment this for development only so that code reloading works
        // TODO: add a CI check for this
        //IFn resolve = RT.var("clojure.core", "requiring-resolve");
        //Symbol logMessageName = Symbol.intern("dialog.logger", "log-message");
        //f = (IFn)resolve.invoke(logMessageName);

        f.invoke(name, level.keyword, msg, err);
    }


    ///// TRACE Methods /////

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(Level.TRACE);
    }


    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isEnabled(Level.TRACE);
    }


    @Override
    public void trace(String msg) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, msg, null);
        }
    }


    @Override
    public void trace(String format, Object arg) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, String.format(format, arg), null);
        }
    }


    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void trace(String format, Object... args) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, String.format(format, args), null);
        }
    }


    @Override
    public void trace(String msg, Throwable err) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, msg, err);
        }
    }


    @Override
    public void trace(Marker marker, String msg) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, msg, null);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, String.format(format, arg), null);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void trace(Marker marker, String format, Object... args) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, String.format(format, args), null);
        }
    }


    @Override
    public void trace(Marker marker, String msg, Throwable err) {
        if (isEnabled(Level.TRACE)) {
            logMessage(Level.TRACE, msg, err);
        }
    }


    ///// DEBUG Methods /////

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }


    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isEnabled(Level.DEBUG);
    }


    @Override
    public void debug(String msg) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, msg, null);
        }
    }


    @Override
    public void debug(String format, Object arg) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, String.format(format, arg), null);
        }
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void debug(String format, Object... args) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, String.format(format, args), null);
        }
    }


    @Override
    public void debug(String msg, Throwable err) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, msg, err);
        }
    }


    @Override
    public void debug(Marker marker, String msg) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, msg, null);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, String.format(format, arg), null);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void debug(Marker marker, String format, Object... args) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, String.format(format, args), null);
        }
    }


    @Override
    public void debug(Marker marker, String msg, Throwable err) {
        if (isEnabled(Level.DEBUG)) {
            logMessage(Level.DEBUG, msg, err);
        }
    }


    ///// INFO Methods /////

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }


    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isEnabled(Level.INFO);
    }


    @Override
    public void info(String msg) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, msg, null);
        }
    }


    @Override
    public void info(String format, Object arg) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, String.format(format, arg), null);
        }
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void info(String format, Object... args) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, String.format(format, args), null);
        }
    }


    @Override
    public void info(String msg, Throwable err) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, msg, err);
        }
    }


    @Override
    public void info(Marker marker, String msg) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, msg, null);
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, String.format(format, arg), null);
        }
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void info(Marker marker, String format, Object... args) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, String.format(format, args), null);
        }
    }


    @Override
    public void info(Marker marker, String msg, Throwable err) {
        if (isEnabled(Level.INFO)) {
            logMessage(Level.INFO, msg, err);
        }
    }


    ///// WARN Methods /////

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }


    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isEnabled(Level.WARN);
    }


    @Override
    public void warn(String msg) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, msg, null);
        }
    }


    @Override
    public void warn(String format, Object arg) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, String.format(format, arg), null);
        }
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void warn(String format, Object... args) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, String.format(format, args), null);
        }
    }


    @Override
    public void warn(String msg, Throwable err) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, msg, err);
        }
    }


    @Override
    public void warn(Marker marker, String msg) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, msg, null);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, String.format(format, arg), null);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void warn(Marker marker, String format, Object... args) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, String.format(format, args), null);
        }
    }


    @Override
    public void warn(Marker marker, String msg, Throwable err) {
        if (isEnabled(Level.WARN)) {
            logMessage(Level.WARN, msg, err);
        }
    }


    ///// ERROR Methods /////

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
    }


    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isEnabled(Level.ERROR);
    }


    @Override
    public void error(String msg) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, msg, null);
        }
    }


    @Override
    public void error(String format, Object arg) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, String.format(format, arg), null);
        }
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void error(String format, Object... args) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, String.format(format, args), null);
        }
    }


    @Override
    public void error(String msg, Throwable err) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, msg, err);
        }
    }


    @Override
    public void error(Marker marker, String msg) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, msg, null);
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, String.format(format, arg), null);
        }
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, String.format(format, arg1, arg2), null);
        }
    }


    @Override
    public void error(Marker marker, String format, Object... args) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, String.format(format, args), null);
        }
    }


    @Override
    public void error(Marker marker, String msg, Throwable err) {
        if (isEnabled(Level.ERROR)) {
            logMessage(Level.ERROR, msg, err);
        }
    }

}
