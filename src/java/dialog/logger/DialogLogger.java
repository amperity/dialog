package dialog.logger;

import org.slf4j.Logger;
import org.slf4j.Marker;


/**
 * Logger interface for integrating with SLF4J.
 */
public final class DialogLogger implements Logger {

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


    @Override
    public String getName() {
        return name;
    }


    ///// TRACE Methods /////

    @Override
    public boolean isTraceEnabled() {
        // TODO: implement
    }


    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }


    @Override
    public void trace(String msg) {
        // TODO: implement
    }


    @Override
    public void trace(String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void trace(String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void trace(String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void trace(String msg, Throwable t) {
        // TODO: implement
    }


    @Override
    public void trace(Marker marker, String msg) {
        // TODO: implement
    }


    @Override
    public void trace(Marker marker, String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void trace(Marker marker, String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        // TODO: implement
    }


    ///// DEBUG Methods /////

    @Override
    public boolean isDebugEnabled() {
        // TODO: implement
    }


    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }


    @Override
    public void debug(String msg) {
        // TODO: implement
    }


    @Override
    public void debug(String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void debug(String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void debug(String msg, Throwable t) {
        // TODO: implement
    }


    @Override
    public void debug(Marker marker, String msg) {
        // TODO: implement
    }


    @Override
    public void debug(Marker marker, String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void debug(Marker marker, String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        // TODO: implement
    }


    ///// INFO Methods /////

    @Override
    public boolean isInfoEnabled() {
        // TODO: implement
    }


    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }


    @Override
    public void info(String msg) {
        // TODO: implement
    }


    @Override
    public void info(String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void info(String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void info(String msg, Throwable t) {
        // TODO: implement
    }


    @Override
    public void info(Marker marker, String msg) {
        // TODO: implement
    }


    @Override
    public void info(Marker marker, String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void info(Marker marker, String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void info(Marker marker, String msg, Throwable t) {
        // TODO: implement
    }


    ///// WARN Methods /////

    @Override
    public boolean isWarnEnabled() {
        // TODO: implement
    }


    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }


    @Override
    public void warn(String msg) {
        // TODO: implement
    }


    @Override
    public void warn(String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void warn(String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void warn(String msg, Throwable t) {
        // TODO: implement
    }


    @Override
    public void warn(Marker marker, String msg) {
        // TODO: implement
    }


    @Override
    public void warn(Marker marker, String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void warn(Marker marker, String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        // TODO: implement
    }


    ///// ERROR Methods /////

    @Override
    public boolean isErrorEnabled() {
        // TODO: implement
    }


    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }


    @Override
    public void error(String msg) {
        // TODO: implement
    }


    @Override
    public void error(String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void error(String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void error(String msg, Throwable t) {
        // TODO: implement
    }


    @Override
    public void error(Marker marker, String msg) {
        // TODO: implement
    }


    @Override
    public void error(Marker marker, String format, Object arg) {
        // TODO: implement
    }


    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        // TODO: implement
    }


    @Override
    public void error(Marker marker, String format, Object... args) {
        // TODO: implement
    }


    @Override
    public void error(Marker marker, String msg, Throwable t) {
        // TODO: implement
    }

}
