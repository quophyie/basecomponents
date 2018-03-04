package com.quantal.javashared.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.godaddy.logging.logger.AnnotatingLogger;
import com.godaddy.logging.logger.LoggerImpl;
import com.quantal.javashared.constants.CommonConstants;
import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.dto.LogEvent;
import com.quantal.javashared.dto.LogzioConfig;
import com.quantal.javashared.exceptions.EventNotSuppliedException;
import io.logz.sender.LogzioSender;
import io.logz.sender.com.google.gson.JsonNull;
import io.logz.sender.com.google.gson.JsonObject;
import io.logz.sender.exceptions.LogzioParameterErrorException;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.quantal.javashared.constants.CommonConstants.EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.MARKER_KER;
import static com.quantal.javashared.constants.CommonConstants.SUB_EVENT_KEY;


/**
 * Created by dman on 19/07/2017.
 */
public class QuantalGoDaddyLoggerImpl extends LoggerImpl implements QuantalLogger {

    protected boolean hasEvent;
    protected LogzioSender sender;
    protected LogzioConfig logzioConfig;
    private ObjectMapper jsonObjectMapper;

    private final String EVENT_MSG="Event not supplied. Please supply an event via the %s method or with an 'event' key in the 'with' method";
    private boolean bSendToLogzio = false;
    private Map<String, Object> logzioJsonDataMap = new HashMap<>();
    private JsonObject jsonMessage;
    private Map<String, Object> commonFieldsMap;
    private CommonLogFields commonLogFields;


    public QuantalGoDaddyLoggerImpl(Logger root, LoggingConfigs configs, LogzioConfig logzioConfig) {
        super(root, configs);
        this.logzioConfig = logzioConfig;
        this.configs = configs;
        this.jsonObjectMapper = new ObjectMapper();
        commonFieldsMap = new HashMap<>();

        if (logzioConfig != null)
            bSendToLogzio = true;

        if(bSendToLogzio) {
            try {
                sender = LogzioSender.getOrCreateSenderByType(logzioConfig.getLogzioToken(),
                        logzioConfig.getLogzioType(),
                        logzioConfig.getDrainTimeout(),
                        logzioConfig.getFsPercentThreshold(),
                        logzioConfig.getBufferDir(),
                        logzioConfig.getLogzioUrl(),
                        logzioConfig.getSocketTimeout(),
                        logzioConfig.getConnectTimeout(),
                        logzioConfig.isDebug(),
                        logzioConfig.getReporter(), logzioConfig.getTasksExecutor(),
                        logzioConfig.getGcPersistedQueueFilesIntervalSeconds());
                sender.start();
            } catch (LogzioParameterErrorException e) {
                root.error(e.getMessage(), e);
            }

        }


    }

    public QuantalGoDaddyLoggerImpl(Logger root, LoggingConfigs configs) {
        this(root, configs, QuantalLoggerFactory.createDefaultLogzioConfig("LOGIO_TOKEN", Optional.empty(), Optional.empty()));

    }

    @Override
    public Logger with(Object obj) {
        if(obj instanceof LogEvent && !StringUtils.isEmpty(((LogEvent) obj).getEvent())){

            this.hasEvent = true;
        }

        JsonObject quantalLoggerJsonMessage = new JsonObject();
        if (obj instanceof Map){
            quantalLoggerJsonMessage = createJsonMessageFromList(Arrays.asList(obj));
        }
        else {
            addToLogzioDataMap(obj);
            quantalLoggerJsonMessage = createJsonMessageFromList(Arrays.asList(obj));
            quantalLoggerJsonMessage = updateJsonMessage(this.jsonMessage, quantalLoggerJsonMessage);
        }
        Logger logger =  new AnnotatingLogger(root, this, obj, configs);
        QuantalLogger quantalLogger = createImmutableQuantalGoDaddyLogger(logger, quantalLoggerJsonMessage, this.hasEvent, this.commonFieldsMap);
        return quantalLogger;
    }

    @Override
    public Logger with(final String key, final Object value) {
        if (!StringUtils.isEmpty(key) && !this.hasEvent) {

            if (!CommonConstants.EVENT_KEY.equals(key.toLowerCase().trim())) {
                hasEvent = false;
            } else {
                hasEvent = true;

            }
        }

        if (jsonMessage == null)
            this.jsonMessage = new JsonObject();
         //jsonMessage.addProperty(key, value == null ? null : value.toString());
        JsonObject quantalLoggerjsonMsg =  new JsonObject();
        quantalLoggerjsonMsg.addProperty(key, value == null ? null : value.toString());
        this.jsonMessage.entrySet().forEach((entry) -> {
            quantalLoggerjsonMsg.addProperty(entry.getKey(), entry.getValue() == null ? null :  entry.getValue().getAsString());
        });
        Logger logger = super.with(key, value);
        QuantalLogger quantalLogger = createImmutableQuantalGoDaddyLogger(logger, quantalLoggerjsonMsg, this.hasEvent, this.commonFieldsMap);
        return quantalLogger;
    }

    @Override
    public void trace(String msg) {
        checkAndMaybeSendToELK(msg,"trace", null);
        super.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        checkAndMaybeSendToELK(format,"trace", Arrays.asList(arg));
        super.trace(format, arg);
    }


    @Override
    public void trace(String format, Object arg1, Object arg2) {
        checkAndMaybeSendToELK(format,"trace", Arrays.asList(arg1, arg2));
        super.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object... arguments) {
        checkAndMaybeSendToELK(format,"trace", Arrays.asList(arguments));
        super.trace(format, arguments);
    }

    @Override
    public void trace(String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())).jsonMessage;
        checkAndMaybeSendToELK(msg,"trace", Arrays.asList(t));
        super.trace(msg, t);
    }


    @Override
    public void trace(Marker marker, String msg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"trace", null);
        super.trace(marker,msg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"trace", Arrays.asList(arg));
        super.trace(marker, format, arg);
    }

    @Override
    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"trace", Arrays.asList(arg1, arg2));
        super.trace(marker, format, arg1, arg2);
    }

    @Override
    public void trace(Marker marker, String format, Object... argArray) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"trace", Arrays.asList(argArray));
        super.trace(marker, format, argArray);
    }

    @Override
    public void trace(Marker marker, String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())
                .with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"trace", Arrays.asList(t));
        super.trace(marker,msg, t);

    }


    @Override
    public void debug(String msg) {
        checkAndMaybeSendToELK(msg,"debug", null);
        super.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        checkAndMaybeSendToELK(format,"debug", Arrays.asList(arg));
        super.debug(format, arg);
    }


    @Override
    public void debug(String format, Object arg1, Object arg2) {
        checkAndMaybeSendToELK(format,"debug", Arrays.asList(arg1, arg2));
        super.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object... arguments) {
        checkAndMaybeSendToELK(format,"debug", Arrays.asList(arguments));
        super.debug(format, arguments);
    }

    @Override
    public void debug(String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())).jsonMessage;
        checkAndMaybeSendToELK(msg,"debug", Arrays.asList(t));
        super.debug(msg, t);
    }


    @Override
    public void debug(Marker marker, String msg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"debug",null);;
        super.debug(marker, msg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"debug", Arrays.asList(arg));
        super.debug(marker, format, arg);
    }

    @Override
    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"debug", Arrays.asList(arg1, arg2));
        super.debug(marker, format, arg1, arg2);
    }

    @Override
    public void debug(Marker marker, String format, Object... argArray) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"debug", Arrays.asList(argArray));
        super.debug(marker, format, argArray);
    }

    @Override
    public void debug(Marker marker, String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())
                .with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"debug", Arrays.asList(t));
        super.debug(marker,msg, t);
    }


/*******/


@Override
public void info(String msg) {
    checkAndMaybeSendToELK(msg,"info", null);
    super.info(msg);
}

    @Override
    public void info(String format, Object arg) {
        checkAndMaybeSendToELK(format,"info", Arrays.asList(arg));
        super.info(format, arg);
    }


    @Override
    public void info(String format, Object arg1, Object arg2) {
        checkAndMaybeSendToELK(format,"info", Arrays.asList(arg1, arg2));
        super.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object... arguments) {
        checkAndMaybeSendToELK(format,"info", Arrays.asList(arguments));
        super.info(format, arguments);
    }

    @Override
    public void info(String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())).jsonMessage;
        checkAndMaybeSendToELK(msg,"info", Arrays.asList(t));
        super.info(msg, t);
    }


    @Override
    public void info(Marker marker, String msg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"info",null);
        super.info(marker, msg);
    }

    @Override
    public void info(Marker marker, String format, Object arg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"info", Arrays.asList(arg));
        super.info(marker, format, arg);
    }

    @Override
    public void info(Marker marker, String format, Object arg1, Object arg2) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"info", Arrays.asList(arg1, arg2));
        super.info(marker, format, arg1, arg2);
    }

    @Override
    public void info(Marker marker, String format, Object... argArray) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"info", Arrays.asList(argArray));
        super.info(marker, format, argArray);
    }

    @Override
    public void info(Marker marker, String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())
                .with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"info", Arrays.asList(t));
        super.info(marker,msg, t);
    }


    /**** WARN **/
    @Override
    public void warn(String msg) {
        checkAndMaybeSendToELK(msg,"warn", null);
        super.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        checkAndMaybeSendToELK(format,"warn", Arrays.asList(arg));
        super.warn(format, arg);
    }


    @Override
    public void warn(String format, Object arg1, Object arg2) {
        checkAndMaybeSendToELK(format,"warn", Arrays.asList(arg1, arg2));
        super.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String format, Object... arguments) {
        checkAndMaybeSendToELK(format,"warn", Arrays.asList(arguments));
        super.warn(format, arguments);
    }

    @Override
    public void warn(String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())).jsonMessage;
        checkAndMaybeSendToELK(msg,"warn", Arrays.asList(t));
        super.warn(msg, t);
    }


    @Override
    public void warn(Marker marker, String msg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"warn",null);
        super.warn(marker, msg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"warn", Arrays.asList(arg));
        super.warn(marker, format, arg);
    }

    @Override
    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"warn", Arrays.asList(arg1, arg2));
        super.warn(marker, format, arg1, arg2);
    }

    @Override
    public void warn(Marker marker, String format, Object... argArray) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"warn", Arrays.asList(argArray));
        super.warn(marker, format, argArray);
    }

    @Override
    public void warn(Marker marker, String msg, Throwable t) {
        this.with(EVENT_KEY, t.getClass().getName())
                .with(MARKER_KER, marker);
        checkAndMaybeSendToELK(msg,"warn", Arrays.asList(t));
        super.warn(marker,msg, t);
    }

    /*** END OF WARN ***/

    /**
     *
     * ERROR
     */
    @Override
    public void error(String msg) {
        checkAndMaybeSendToELK(msg,"error", null);
        super.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        checkAndMaybeSendToELK(format,"error", Arrays.asList(arg));
        super.error(format, arg);
    }


    @Override
    public void error(String format, Object arg1, Object arg2) {
        checkAndMaybeSendToELK(format,"error", Arrays.asList(arg1, arg2));
        super.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object... arguments) {
        checkAndMaybeSendToELK(format,"error", Arrays.asList(arguments));
        super.error(format, arguments);
    }

    @Override
    public void error(String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())).jsonMessage;
        checkAndMaybeSendToELK(msg,"error", Arrays.asList(t));
        super.error(msg, t);
    }


    @Override
    public void error(Marker marker, String msg) {
        this.jsonMessage= ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"error",null);;
        super.error(marker, msg);
    }

    @Override
    public void error(Marker marker, String format, Object arg) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(format,"error", Arrays.asList(arg));
        super.error(marker, format, arg);
    }

    @Override
    public void error(Marker marker, String format, Object arg1, Object arg2) {
        this.with(MARKER_KER, marker);
        checkAndMaybeSendToELK(format,"error", Arrays.asList(arg1, arg2));
        super.error(marker, format, arg1, arg2);
    }

    @Override
    public void error(Marker marker, String format, Object... argArray) {
        this.with(MARKER_KER, marker);
        checkAndMaybeSendToELK(format,"error", Arrays.asList(argArray));
        super.error(marker, format, argArray);
    }

    @Override
    public void error(Marker marker, String msg, Throwable t) {
        this.jsonMessage = ((QuantalGoDaddyLoggerImpl)this.with(EVENT_KEY, t.getClass().getName())
            .with(MARKER_KER, marker)).jsonMessage;
        checkAndMaybeSendToELK(msg,"error", Arrays.asList(t));
        super.error(marker,msg, t);
    }

    @Override
    public void error(Throwable t, String format, Object... args) {
        checkAndMaybeSendToELK(format,"error", Arrays.asList(args));
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.error(ft.getMessage(), t);
    }

    @Override
    public void warn(final Throwable t, final String format, final Object... args) {
        checkAndMaybeSendToELK(format,"warn", Arrays.asList(args));
        FormattingTuple ft = MessageFormatter.arrayFormat(format, args);
        root.warn(ft.getMessage(), t);
    }

    @Override public void success(final String format, final Object... args) {
        info(successMarker, format, args);
    }

    @Override public void dashboard(final String format, final Object... args) {
        info(dashboardMarker, format, args);
    }

    private void checkAndMaybeSendToELK(String msg, String methodName, List<Object> arguments){
        checkAndMaybeThrowEventNotSuppliedException(methodName, arguments);
        checkAndSendToLogzio(msg, arguments);
    }

    private void checkAndMaybeThrowEventNotSuppliedException(String methodName, List<Object> arguments) {

        if (arguments != null) {
            //Object event = arguments.stream().filter(arg -> arg instanceof LogEvent).findAny().orElse(null);
            LogEvent event = tryGetEvent(arguments);
            LogEvent subEvent = tryGetSubEvent(arguments);

            if (!this.hasEvent && event == null) {
                sendEventNotSuppliedExceptionToLogzioAndThrow(methodName);
            }

            if (event != null) {
                this.with(EVENT_KEY, event == null ? null : event.getEvent());
                this.with(SUB_EVENT_KEY, subEvent == null ? null : subEvent.getEvent());
            }
        } else if (!this.hasEvent) {
            sendEventNotSuppliedExceptionToLogzioAndThrow(methodName);
        }
    }

    private void checkAndSendToLogzio(String msg, List<Object> args) {
        if (!bSendToLogzio)
            return;

        LogEvent event = tryGetEvent(args);
        LogEvent subEvent = tryGetSubEvent(args);
        logzioJsonDataMap.put(EVENT_KEY,event == null ? null : event.getEvent());
        logzioJsonDataMap.put(SUB_EVENT_KEY, subEvent == null ? null : subEvent.getEvent());

        Object[] argsArr = args == null ? null : args.toArray();
        String formattedMsg = getFormattedMessage(msg, argsArr);

        JsonObject argsJsonMessage = createJsonMessageFromList(args);
        logzioJsonDataMap.putAll(commonFieldsMap);
        logzioJsonDataMap.putIfAbsent("msg", formattedMsg);
        //logzioJsonDataMap.putIfAbsent("message", formattedMsg);

        if (args != null) {
            args.forEach(this::addToLogzioDataMap);
        }
        JsonObject logzioJsonDataMapMessage = createJsonMessageFromList(Arrays.asList(logzioJsonDataMap));
        JsonObject logzioDataAndArgsJsonMessage = updateJsonMessage(argsJsonMessage, logzioJsonDataMapMessage);
        JsonObject jsonMessageToSendToLogzio = updateJsonMessage(this.jsonMessage, logzioDataAndArgsJsonMessage);
        sender.send(jsonMessageToSendToLogzio);

        resetDataContainers();
    }

    private void addToLogzioDataMap(Object obj){
        String key = obj.getClass().getSimpleName();

        if(!logzioJsonDataMap.containsKey(key)){
            Matcher matcher =  Pattern.compile("\\d*$").matcher(key);
            if (matcher.find()){
                String group = matcher.group();
                long objTypeCnt = StringUtils.isEmpty(group) ? 0L : Long.valueOf(group);
                String newKey = key.substring(0, group.indexOf(group)).concat(String.valueOf(++objTypeCnt));
                if(obj instanceof LogEvent) {
                    logzioJsonDataMap.putIfAbsent(EVENT_KEY, ((LogEvent)obj).getEvent());
                } else {
                    logzioJsonDataMap.put(key.concat(newKey), obj);
                }

            }
        }
    }

    private JsonObject createJsonMessageFromList(List<Object> args){

        final JsonObject jsonMessage = new JsonObject();
        if (args != null ) {
            args.stream().forEach(arg -> {
                if (arg instanceof Map) {

                    ((Map) arg).entrySet().forEach((entry) -> {
                        //String value = new Gson().toJson(((Map.Entry<String, Object>) entry).getValue());
                        Object value = ((Map.Entry<String, Object>) entry).getValue()!= null ?((Map.Entry<String, Object>) entry).getValue().toString() :"";
                        jsonMessage.addProperty(((Map.Entry<String, Object>) entry).getKey(), value.toString());
                    });
                }
            });
        }
        return jsonMessage;
    }

    private JsonObject updateJsonMessage(JsonObject source, JsonObject target){
        JsonObject jsonObject = new JsonObject();
        if (source == null && target != null){
            target.entrySet().forEach(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue().getAsString()));
        } else if (source != null && target == null){
            source.entrySet().forEach(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue().getAsString()));
        } else {
            source.entrySet().forEach(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue().getAsString()));
            target.entrySet().forEach(entry -> jsonObject.addProperty(entry.getKey(), entry.getValue().getAsString()));

        }
        return jsonObject;
    }

    private JsonObject createLogMessage(){
        jsonMessage =  new JsonObject();
        return jsonMessage;

    }

    private String getFormattedMessage(String msg, Object... args){
        return MessageFormatter.arrayFormat(msg, args).getMessage();
    }

    @Override
    public CommonLogFields getCommoFields() {
        return this.commonLogFields;
    }

    @Override
    public void setCommoFields(CommonLogFields commonLogFields) {
        this.commonLogFields = commonLogFields;
        Arrays.asList(ReflectionUtils.getAllDeclaredMethods(commonLogFields.getClass()))
                .stream()
                .filter(method -> method.getName().startsWith("get") &&  !method.getName().equalsIgnoreCase("getClass"))
                .forEach(method -> {
                    try {
                        String key = method.getName().substring(3).toLowerCase();
                        Object value = method.invoke(commonLogFields);
                        commonFieldsMap.put(key, value);
                    } catch (IllegalAccessException | InvocationTargetException  e) {
                       root.error(e.getMessage(), e );
                    }
                });
    }

    private QuantalLogger createImmutableQuantalGoDaddyLogger(Logger logger, JsonObject jsonMessage, boolean hasEvent, Map<String, Object> commonFieldsMap){
        QuantalLogger quantalLogger =  new QuantalGoDaddyLoggerImpl(logger, this.configs, logzioConfig);
        ((QuantalGoDaddyLoggerImpl)quantalLogger).setCommonFieldsMap(commonFieldsMap);
        ((QuantalGoDaddyLoggerImpl)quantalLogger).setHasEvent(hasEvent);
        ((QuantalGoDaddyLoggerImpl)quantalLogger).setJsonMessage(jsonMessage);
        return quantalLogger;
    }

    private LogEvent tryGetSubEvent(List<Object> args){
        if(this.logzioJsonDataMap != null && this.logzioJsonDataMap.get(SUB_EVENT_KEY) != null){
            return new LogEvent(this.logzioJsonDataMap.get(SUB_EVENT_KEY).toString());
        } else if (this.jsonMessage != null && this.jsonMessage.get(SUB_EVENT_KEY) != null && this.jsonMessage.get(SUB_EVENT_KEY) != JsonNull.INSTANCE){
             return new LogEvent(this.jsonMessage.get(SUB_EVENT_KEY).getAsString());
        }
        Object subEvent = null;
        if (args != null ) {
             subEvent = args.stream().filter(arg -> (arg instanceof LogEvent) && ((LogEvent) arg).getEvent().equalsIgnoreCase(CommonConstants.SUB_EVENT_KEY)).findAny().orElse(null);
            /*if(subEvent == null){
                if(this.logzioJsonDataMap != null && !this.logzioJsonDataMap.isEmpty()){
                    subEvent = this.logzioJsonDataMap.get(EVENT_KEY);
                } else if (this.jsonMessage != null && !this.jsonMessage.entrySet().isEmpty()){
                    subEvent = this.jsonMessage.get(EVENT_KEY);
                }
            }*/

            // If we still cant find the sub event, try and set the the subEvent to the event
            if (subEvent == null) {
                try {
                    MDC mdc = (MDC) args.stream().filter(arg -> arg instanceof MDC)
                            .findAny()
                            .orElseThrow(() -> new NullPointerException("Mdc null"));
                    if (mdc.get(EVENT_KEY) != null) {
                        subEvent = mdc.get(EVENT_KEY);
                    }
                } catch (NullPointerException npe) {
                    subEvent = subEvent;
                }
            }
        }

        // Return th
        if(subEvent instanceof LogEvent || subEvent == null) {
            if (subEvent == null)
                return  null;
            if (subEvent !=null)
                return (LogEvent) subEvent;
            if (this.jsonMessage.get(EVENT_KEY) != null)
                return new LogEvent(this.jsonMessage.get(EVENT_KEY).toString());
            else  if (this.logzioJsonDataMap.get(EVENT_KEY) != null)
                return new LogEvent(this.logzioJsonDataMap.get(EVENT_KEY).toString());
        }

        return new LogEvent(subEvent.toString());
    }

    private LogEvent tryGetEvent(List<Object> args){
        if(this.logzioJsonDataMap != null && this.logzioJsonDataMap.get(EVENT_KEY) != null){
            return new LogEvent(this.logzioJsonDataMap.get(EVENT_KEY).toString());
        } else if (this.jsonMessage != null && this.jsonMessage.get(EVENT_KEY) != null && this.jsonMessage.get(EVENT_KEY) != JsonNull.INSTANCE){
            return new LogEvent(this.jsonMessage.get(EVENT_KEY).getAsString());
        }
        Object event = null;
        if (args != null ) {
            event = args.stream().filter(arg -> (arg instanceof LogEvent) && ((LogEvent) arg).getEvent().equalsIgnoreCase(CommonConstants.EVENT_KEY)).findAny().orElse(null);
            if(event == null){
                if(this.logzioJsonDataMap != null && !this.logzioJsonDataMap.isEmpty()){
                    event = this.logzioJsonDataMap.get(EVENT_KEY);
                } else if (this.jsonMessage != null && !this.jsonMessage.entrySet().isEmpty()){
                    event = this.jsonMessage.get(EVENT_KEY);
                }
            }

            // If we still cant find the sub event, try and set the the event to the event
            if (event == null) {
                try {
                    MDC mdc = (MDC) args.stream().filter(arg -> arg instanceof MDC)
                            .findAny()
                            .orElseThrow(() -> new NullPointerException("Mdc null"));
                    if (mdc.get(EVENT_KEY) != null) {
                        event = mdc.get(EVENT_KEY);
                    }
                } catch (NullPointerException npe) {
                    event = event;
                }
            }
        }

        if(event instanceof LogEvent || event == null)
            return  event == null ? null : (LogEvent) event;

        return new LogEvent(event.toString());
    }

    private void resetDataContainers(){
        this.jsonMessage = createLogMessage();
        this.logzioJsonDataMap = new HashMap<>();
    }
    private void sendEventNotSuppliedExceptionToLogzioAndThrow(String methodName){
        String message = String.format(EVENT_MSG, methodName);
        RuntimeException exception = new EventNotSuppliedException(message);
        try {
            this.with(EVENT_KEY, exception.getClass().getName())
            .with("msg", exception.getMessage())
            .with("stack", jsonObjectMapper.writeValueAsString(exception.getStackTrace()));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        logzioJsonDataMap.put("Test", "the test key");
        sender.send(jsonMessage);
        super.error(message, exception);
        resetDataContainers();
        throw new EventNotSuppliedException(message);
    }

    public void setHasEvent(boolean hasEvent){
        this.hasEvent = hasEvent;
    }

    public void setCommonFieldsMap(Map<String, Object> commonFieldsMap){
        this.commonFieldsMap = commonFieldsMap;
    }

    public void setJsonMessage(JsonObject jsonMessage){
        this.jsonMessage = jsonMessage;
    }
}
