package com.quantal.shared.logger;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.godaddy.logging.logger.AnnotatingLogger;
import com.godaddy.logging.logger.LoggerImpl;
import com.quantal.shared.dto.LogEvent;
import com.quantal.shared.exceptions.EventNotSuppliedException;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;


/**
 * Created by dman on 19/07/2017.
 */
public class QuantalGoDaddyLoggerImpl extends LoggerImpl implements QuantalGoDaddyLogger {

    protected boolean hasEvent;

    private final String EVENT_MSG="Event not supplied. Please supply an event via the %s method or with an 'event' key in the 'with' method";
    private final String EVENT_KEY = "event";

    public QuantalGoDaddyLoggerImpl(Logger root, LoggingConfigs configs) {
        super(root, configs);

    }

    @Override
    public Logger with(Object obj) {
        if(obj instanceof LogEvent && !StringUtils.isEmpty(((LogEvent) obj).getEvent())){
            this.hasEvent = true;
        }
        return new AnnotatingLogger(root, this, obj, configs);
    }

    @Override
    public Logger with(final String key, final Object value) {
        if (!StringUtils.isEmpty(key)) {

            if(!"event".equals(key.toLowerCase().trim())) {
                hasEvent = false;
            } else {
                hasEvent = true;
            }
        }
            return super.with(key, value);
    }

    @Override
    public void trace(String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("trace", null);
        super.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("trace", Arrays.asList(arg));
        super.trace(format, arg);
    }


    @Override
    public void trace(String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("trace", Arrays.asList(arg1, arg2));
        super.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        checkAndMaybeThrowEeventNotSuppliedException("trace", Arrays.asList(arguments));
        super.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
        .trace(msg, t);
    }


    @Override
    public void trace(Marker marker, String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("trace",null);;
        super.trace(marker, msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("trace", Arrays.asList(arg));
        super.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("trace", Arrays.asList(arg1, arg2));
        super.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        checkAndMaybeThrowEeventNotSuppliedException("trace", Arrays.asList(argArray));
        super.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .trace(marker,msg, t);
    }


    @Override
    public void debug(String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("debug", null);
        super.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("debug", Arrays.asList(arg));
        super.debug(format, arg);
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("debug", Arrays.asList(arg1, arg2));
        super.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        checkAndMaybeThrowEeventNotSuppliedException("debug", Arrays.asList(arguments));
        super.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .debug(msg, t);
    }


    @Override
    public void debug(Marker marker, String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("debug",null);;
        super.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("debug", Arrays.asList(arg));
        super.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("debug", Arrays.asList(arg1, arg2));
        super.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        checkAndMaybeThrowEeventNotSuppliedException("debug", Arrays.asList(argArray));
        super.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .debug(marker,msg, t);
    }


/*******/


@Override
public void info(String msg) {
    checkAndMaybeThrowEeventNotSuppliedException("info", null);
    super.info(msg);
}

    @Override
    public void info(String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("info", Arrays.asList(arg));
        super.info(format, arg);
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("info", Arrays.asList(arg1, arg2));
        super.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        checkAndMaybeThrowEeventNotSuppliedException("info", Arrays.asList(arguments));
        super.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .info(msg, t);
    }


    @Override
    public void info(Marker marker, String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("info",null);;
        super.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("info", Arrays.asList(arg));
        super.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("info", Arrays.asList(arg1, arg2));
        super.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        checkAndMaybeThrowEeventNotSuppliedException("info", Arrays.asList(argArray));
        super.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .info(marker,msg, t);
    }


    /**** WARN **/
    @Override
    public void warn(String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", null);
        super.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", Arrays.asList(arg));
        super.warn(format, arg);
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", Arrays.asList(arg1, arg2));
        super.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", Arrays.asList(arguments));
        super.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .warn(msg, t);
    }


    @Override
    public void warn(Marker marker, String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("warn",null);;
        super.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", Arrays.asList(arg));
        super.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", Arrays.asList(arg1, arg2));
        super.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", Arrays.asList(argArray));
        super.warn(marker, format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .warn(marker,msg, t);
    }

    /*** END OF WARN ***/

    /**
     *
     * ERROR
     */
    @Override
    public void error(String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("error", null);
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("error", Arrays.asList(arg));
        super.error(format, arg);
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("error", Arrays.asList(arg1, arg2));
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        checkAndMaybeThrowEeventNotSuppliedException("error", Arrays.asList(arguments));
        super.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .error(msg, t);
    }


    @Override
    public void error(Marker marker, String msg) {
        checkAndMaybeThrowEeventNotSuppliedException("error",null);;
        super.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        checkAndMaybeThrowEeventNotSuppliedException("error", Arrays.asList(arg));
        super.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        checkAndMaybeThrowEeventNotSuppliedException("error", Arrays.asList(arg1, arg2));
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        checkAndMaybeThrowEeventNotSuppliedException("error", Arrays.asList(argArray));
        super.error(marker, format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .error(marker,msg, t);
    }

    @Override
    public void error(Throwable t, String format, Object... args) {
        checkAndMaybeThrowEeventNotSuppliedException("error", Arrays.asList(args));
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.error(ft.getMessage(), t);
    }

    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
        checkAndMaybeThrowEeventNotSuppliedException("warn", Arrays.asList(args));
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.warn(ft.getMessage(), t);
    }

    @Override public void success(final String format, final Object... args) {
        info(successMarker, format, args);
    }

    @Override public void dashboard(final String format, final Object... args) {
        info(dashboardMarker, format, args);
    }

    private void checkAndMaybeThrowEeventNotSuppliedException(String methodName, List<Object> arguments){
        if (arguments !=null) {
            Object event = arguments.stream().filter(arg -> arg instanceof LogEvent).findAny().orElse(null);


            if (!this.hasEvent && event == null){
                throw new EventNotSuppliedException(String.format(EVENT_MSG, methodName));
            }
            if(event!=null) {
                this.with(EVENT_KEY, ((LogEvent) event).getEvent());
            }
        }

        else if(!this.hasEvent) {
            throw new EventNotSuppliedException(String.format(EVENT_MSG, methodName));
        }
    }

    class LoggerImpl2 extends LoggerImpl{
        public LoggerImpl2(Logger root, LoggingConfigs configs) {
            super(root, configs);
        }
    }
}
