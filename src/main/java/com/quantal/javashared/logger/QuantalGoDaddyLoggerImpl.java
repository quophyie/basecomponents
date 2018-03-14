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
import com.quantal.javashared.dto.LogField;
import com.quantal.javashared.dto.LoggerConfig;
import com.quantal.javashared.dto.LogzioConfig;
import com.quantal.javashared.exceptions.LogFieldNotSuppliedException;
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
import java.util.Set;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.quantal.javashared.constants.CommonConstants.EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.MARKER_KER;
import static com.quantal.javashared.constants.CommonConstants.SUB_EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.TRACE_ID_MDC_KEY;
import static java.util.stream.Collectors.joining;


/**
 * Created by dman on 19/07/2017.
 */
public class QuantalGoDaddyLoggerImpl extends LoggerImpl implements QuantalLogger {

    //protected boolean hasEvent;
    protected org.slf4j.Logger logger;
    //protected boolean hasAllRequiredFields;
    protected LogzioSender sender;
    //protected LogzioConfig logzioConfig;
    private LoggerConfig loggerConfig;
    private ObjectMapper jsonObjectMapper;

    private final String LOG_FIELD_NOT_FOUND_MSG ="Log field `%s` was not supplied. Please supply the log field `%s` via the '%s' method or via the`%s` key in the 'with' method";
    private boolean bSendToLogzio = false;
    private Map<String, Object> logzioJsonDataMap = new HashMap<>();
    private JsonObject jsonMessage;
    private Map<String, Object> commonFieldsMap;
    private CommonLogFields commonLogFields;


    public QuantalGoDaddyLoggerImpl(Logger root, LoggerConfig loggerConfig) {
        super(root, loggerConfig.getGoDadayLoggerConfig());
        LogzioConfig logzioConfig = loggerConfig.getLogzioConfig();
        this.configs = loggerConfig.getGoDadayLoggerConfig();
        this.jsonObjectMapper = new ObjectMapper();
        this.commonFieldsMap = new HashMap<>();
        this.loggerConfig = loggerConfig;

        if (logzioConfig != null)
            bSendToLogzio = true;

        if (loggerConfig.isEventIsRequired()){
            loggerConfig.getRequiredLogFields().putIfAbsent(EVENT_KEY, (args, dataMap) -> checkIfFieldsExistsInLog(args, EVENT_KEY, dataMap));
        }

        if (loggerConfig.isTraceIdIsRequired()){
            loggerConfig.getRequiredLogFields().putIfAbsent(TRACE_ID_MDC_KEY, (args, dataMap) -> checkIfFieldsExistsInLog(args, TRACE_ID_MDC_KEY, dataMap));
        }

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
        this(root, new LoggerConfig()
                        .builder()
                        .goDadayLoggerConfig( configs)
                        .logzioConfig(QuantalLoggerFactory.createDefaultLogzioConfig("LOGIO_TOKEN", Optional.empty(), Optional.empty()))
                        .build()
                );

    }

    @Override
    public Logger with(Object obj) {
        /*if(obj instanceof LogEvent && !StringUtils.isEmpty(((LogEvent) obj).getEvent())){

            this.hasEvent = true;
        }*/

        if (obj instanceof LogField) {

            //this.hasAllRequiredFields = isAllRequiredFieldsFound(this.loggerConfig.getRequiredLogFields(), Arrays.asList(obj), "").isEmpty();
        }

        JsonObject quantalLoggerJsonMessage = new JsonObject();
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.putAll(this.logzioJsonDataMap);
        addToDataMap(dataMap, obj);
        quantalLoggerJsonMessage = createJsonMessageFromList(Arrays.asList(obj));
        quantalLoggerJsonMessage = updateJsonMessage(this.jsonMessage, quantalLoggerJsonMessage);


        Logger logger = new AnnotatingLogger(this.root, this, obj, configs);
        QuantalLogger quantalLogger = createImmutableQuantalGoDaddyLogger(logger, dataMap, this.commonFieldsMap);
        return quantalLogger;
    }

    @Override
    public Logger with(final String key, final Object value) {
        if (!StringUtils.isEmpty(key) /*&& !this.hasAllRequiredFields*/) {

            /*if (!CommonConstants.EVENT_KEY.equals(key.toLowerCase().trim())) {
                hasEvent = false;
            } else {
                hasEvent = true;

            }*/
            //this.hasAllRequiredFields = isAllRequiredFieldsFound(this.loggerConfig.getRequiredLogFields(), Arrays.asList(new LogField(key, value)), "").isEmpty();
        }

        if (jsonMessage == null)
            this.jsonMessage = new JsonObject();
         //jsonMessage.addProperty(key, value == null ? null : value.toString());
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.putAll(this.logzioJsonDataMap);
        JsonObject quantalLoggerjsonMsg =  new JsonObject();
        quantalLoggerjsonMsg.addProperty(key, value == null ? null : value.toString());
        addToDataMap(dataMap, new LogField(key,  value == null ? null : value.toString()));
        this.jsonMessage.entrySet().forEach((entry) -> {
            quantalLoggerjsonMsg.addProperty(entry.getKey(), entry.getValue() == null ? null :  entry.getValue().getAsString());
            addToDataMap(dataMap, new LogField(entry.getKey(), entry.getValue()));
        });

        Logger logger = super.with(key, value);
        QuantalLogger quantalLogger = createImmutableQuantalGoDaddyLogger(logger,  dataMap, this.commonFieldsMap);
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
        Set<String> requiredFields = loggerConfig
                .getRequiredLogFields()
                .keySet();
        checkAndMaybeThrowLogFieldNotSuppliedException(methodName, arguments, requiredFields);
        checkAndSendToLogzio(msg, arguments);
    }

    /**
     * Will fill try and find the required fields supplied in the requiredFieldNames in one of arguments, MDC
     * or logzioJsonDataMap
     * @param methodName
     * @param arguments
     * @param requiredFieldNames
     */
    private void checkAndMaybeThrowLogFieldNotSuppliedException(String methodName, List<Object> arguments, Set<String> requiredFieldNames) {

        if (!bSendToLogzio) {
            resetDataContainers();
            return;
        }

        if (requiredFieldNames != null) {
            boolean hasAllRequiredFields = isAllRequiredFieldsFound(this.loggerConfig.getRequiredLogFields(), arguments,this.logzioJsonDataMap, "");
            requiredFieldNames.forEach(logFieldName -> {
                if (arguments != null) {
                    //Object logField = arguments.stream().filter(arg -> arg instanceof LogEvent).findAny().orElse(null);
                    LogField logField = tryGetLogField(arguments, logFieldName, this.logzioJsonDataMap);


                    if (!hasAllRequiredFields && logField == null) {
                        sendLogFieldNotSuppliedExceptionToLogzioAndThrow(logFieldName, methodName,arguments );
                    }

                    if (logField != null) {
                        this.with(logFieldName, logField == null ? null : logField.getValue());
                        if (logField.getKey().equalsIgnoreCase(EVENT_KEY)) {
                            LogEvent subEvent = tryGetSubEvent(arguments);
                            this.with(SUB_EVENT_KEY, subEvent == null ? null : subEvent.getEvent());
                        }
                    }
                } else if (!hasAllRequiredFields) {
                    sendLogFieldNotSuppliedExceptionToLogzioAndThrow(logFieldName, methodName, arguments);
                }
            });
        }
    }

    /**
     * Will send message to ELK /LOGZIO. All Data in logzioJsonDataMap and args param
     * and all data in MDC will be  converted to keys in the document sent to ELK / Logzio
     * @param msg
     * @param args
     */
    private void checkAndSendToLogzio(String msg, List<Object> args) {
        if (!bSendToLogzio) {
            resetDataContainers();
            return;
        }

        //LogEvent event = (LogEvent) tryGetLogField(args, CommonConstants.EVENT_KEY);
        //LogTraceId traceId = (LogTraceId) tryGetLogField(args, CommonConstants.TRACE_ID_MDC_KEY);
        LogEvent subEvent = tryGetSubEvent(args);
        //logzioJsonDataMap.put(EVENT_KEY,event == null ? null : event.getEvent());
        //logzioJsonDataMap.put(CommonConstants.TRACE_ID_MDC_KEY, traceId == null ? null : traceId.getTraceId());
        logzioJsonDataMap.put(SUB_EVENT_KEY, subEvent == null ? null : subEvent.getEvent());

        Object[] argsArr = args == null ? null : args.toArray();
        String formattedMsg = getFormattedMessage(msg, argsArr);

        JsonObject argsJsonMessage = createJsonMessageFromList(args);
        logzioJsonDataMap.putAll(commonFieldsMap);
        logzioJsonDataMap.putIfAbsent("msg", formattedMsg);
        MDC.getCopyOfContextMap().forEach((key, value) -> logzioJsonDataMap.putIfAbsent(key, value));
        //logzioJsonDataMap.putIfAbsent("message", formattedMsg);

        if (args != null) {
            args.forEach((arg) -> addToDataMap(logzioJsonDataMap, arg));
        }
        JsonObject logzioJsonDataMapMessage = createJsonMessageFromList(Arrays.asList(logzioJsonDataMap));
        JsonObject logzioDataAndArgsJsonMessage = updateJsonMessage(argsJsonMessage, logzioJsonDataMapMessage);
        JsonObject jsonMessageToSendToLogzio = updateJsonMessage(this.jsonMessage, logzioDataAndArgsJsonMessage);
        sender.send(jsonMessageToSendToLogzio);

        resetDataContainers();
    }

    /**
     * Add the given object to the dataMap
     * @param dataMap
     * @param obj
     */
    private void addToDataMap(Map<String, Object> dataMap, Object obj){
        String key = obj.getClass().getSimpleName();

        if(obj instanceof LogField) {
            dataMap.putIfAbsent(((LogField) obj).getKey(), ((LogField)obj).getValue());
        }
        else if(!dataMap.containsKey(key)){
            Matcher matcher =  Pattern.compile("\\d*$").matcher(key);
            if (matcher.find()){
                String group = matcher.group();
                long objTypeCnt = StringUtils.isEmpty(group) ? 0L : Long.valueOf(group);
                String newKey = key.substring(0, group.indexOf(group)).concat(String.valueOf(++objTypeCnt));
                dataMap.put(key.concat(newKey), obj);
            }
        }
    }

    /**
     * Create JsonObject from the list of args.
     * At the moment, only objects which are of type Map<K,V> which are items of
     * the the args param will be added to the JsonObject that is returned
     * @param args
     * @return
     */
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

    /**
     * Updates the target with the source message non destructively i.e. A new object
     * is returned and source and target objects are left as is
     * @param source
     * @param target
     * @return
     */
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

    /**
     * Returns a SLF4J formatted message for output i.e. returns a message where
     * `{}` placeholders have been replaced with thier correct values
     * @param msg
     * @param args
     * @return
     */
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

    private QuantalLogger createImmutableQuantalGoDaddyLogger(Logger logger, Map<String, Object> dataMap, Map<String, Object> commonFieldsMap){
        QuantalLogger quantalLogger =  new QuantalGoDaddyLoggerImpl(logger, this.loggerConfig);
        ((QuantalGoDaddyLoggerImpl)quantalLogger).setCommonFieldsMap(commonFieldsMap);
        ((QuantalGoDaddyLoggerImpl)quantalLogger).setDataMap(dataMap);
        return quantalLogger;
    }

    /**
     * Special case method that tries to find the a subEvent in
     * one of log methods args, MDC or dataMap
     * @param args
     * @return
     */
    private LogEvent tryGetSubEvent(List<Object> args){
        if(this.logzioJsonDataMap != null && this.logzioJsonDataMap.get(SUB_EVENT_KEY) != null){
            return new LogEvent(this.logzioJsonDataMap.get(SUB_EVENT_KEY).toString());
        } else if (this.jsonMessage != null && this.jsonMessage.get(SUB_EVENT_KEY) != null && this.jsonMessage.get(SUB_EVENT_KEY) != JsonNull.INSTANCE){
             return new LogEvent(this.jsonMessage.get(SUB_EVENT_KEY).getAsString());
        }
        Object subEvent = null;
        if (args != null ) {
             subEvent = args.stream().filter(arg -> (arg instanceof LogEvent) &&
                     ((LogEvent) arg).getEvent()!= null
                     && ((LogEvent) arg).getEvent().equalsIgnoreCase(CommonConstants.SUB_EVENT_KEY)).findAny().orElse(null);
            /*if(subEvent == null){
                if(this.logzioJsonDataMap != null && !this.logzioJsonDataMap.isEmpty()){
                    subEvent = this.logzioJsonDataMap.get(EVENT_KEY);
                } else if (this.jsonMessage != null && !this.jsonMessage.entrySet().isEmpty()){
                    subEvent = this.jsonMessage.get(EVENT_KEY);
                }
            }*/

        }

        // If we still cant find the sub event, try and find it in the MDC
        if (subEvent == null) {
            subEvent = MDC.get(EVENT_KEY);
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

    /**
     * Tris to find the given logFieldName
     * one of log methods args, MDC or dataMap
     * @param args
     * @param logFieldName
     * @param dataMap
     * @return
     */
    private LogField tryGetLogField(List<Object> args, String logFieldName, Map<String, Object> dataMap){
        if(dataMap != null && dataMap.get(logFieldName) != null){
            return new LogField(logFieldName, dataMap.get(logFieldName));
        } else if (this.jsonMessage != null && this.jsonMessage.get(logFieldName) != null && this.jsonMessage.get(logFieldName) != JsonNull.INSTANCE){
            return new LogField(logFieldName,this.jsonMessage.get(logFieldName).getAsString());
        }
        Object logField = null;
        if (args != null ) {
            logField = args.stream().filter(arg -> (arg instanceof LogField) && ((LogField) arg).getKey().toString().equalsIgnoreCase(logFieldName)).findAny().orElse(null);
            if(logField == null){
                if(dataMap != null && !dataMap.isEmpty()){
                    logField = dataMap.get(logFieldName);
                } else if (this.jsonMessage != null && !this.jsonMessage.entrySet().isEmpty()){
                    logField = this.jsonMessage.get(logFieldName);
                }
            }
        }

        // If we still cant find the logField in jsonMessage and logzioJsonDataMap,
        // try and find it in the MDC
        if (logField == null) {
            logField = MDC.get(logFieldName);
        }

        if(logField instanceof LogField || logField == null)
            return  logField == null ? null : (LogField) logField;

        return new LogField(logFieldName,logField.toString());
    }

    private void resetDataContainers(){
        this.jsonMessage = createLogMessage();
        this.logzioJsonDataMap = new HashMap<>();
    }

    /**
     * Will send a message to ELK / Logzio if any of the required fields cannot be
     * found in one of log methods args, MDC or dataMap with a message that specifies
     * the missing fields
     * Will throw exception after sending the message
     * @param logFieldName
     * @param methodName
     * @param arguments
     */
    private void sendLogFieldNotSuppliedExceptionToLogzioAndThrow(String logFieldName, String methodName, List<Object> arguments){
        //String message = String.format(LOG_FIELD_NOT_FOUND_MSG, methodName);
        //String message = constructFieldNotFoundErrorMsg(logFieldName, methodName);
        String message = checkAndReturnAllMissingRequiredFieldsErrors(this.loggerConfig.getRequiredLogFields(), arguments, this.logzioJsonDataMap, methodName)
                .values()
                .stream()
                .collect(joining("\n"));
        JsonObject jsonMessage = null;
        RuntimeException exception = new LogFieldNotSuppliedException(message);
        try {
            Map<String, Object> dataMap = new HashMap<>();
            if (loggerConfig.isAddMissingFieldsToElkLogs()) {
                loggerConfig.getRequiredLogFields().forEach((fieldName, findFunction) -> {
                    LogField logField = tryGetLogField(arguments,fieldName,this.logzioJsonDataMap);
                    dataMap.putIfAbsent(fieldName, logField == null ? null : logField.getValue());
                });
            }
            dataMap.put("msg", exception.getMessage());
            dataMap.put("stack", jsonObjectMapper.writeValueAsString(exception.getStackTrace()));
            jsonMessage = createJsonMessageFromList(Arrays.asList(dataMap));
            /*this.with(logFieldName, exception.getClass().getName())
            .with("msg", exception.getMessage())
            .with("stack", jsonObjectMapper.writeValueAsString(exception.getStackTrace()));
            */
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        sender.send(jsonMessage);
        super.error(message, exception);
        resetDataContainers();
        throw new LogFieldNotSuppliedException(message);
    }


    public void setCommonFieldsMap(Map<String, Object> commonFieldsMap){
        this.commonFieldsMap = commonFieldsMap;
    }

    public void setDataMap(Map<String, Object> dataMap){

        this.logzioJsonDataMap.putAll(dataMap);
    }

    /**
     * Returns true if all the fieldName can be found one MDC, log method args or
     * the supplied dataMap
     * @param args
     * @param fieldName
     * @param dataMap
     * @return
     */
    public boolean checkIfFieldsExistsInLog(List args, String fieldName, Map<String, Object> dataMap){
        LogField field = tryGetLogField(args, fieldName, dataMap);
        if (field == null && this.logzioJsonDataMap != null){
            field = (LogField) this.logzioJsonDataMap
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getKey().equalsIgnoreCase(fieldName))
                    .map(entry -> entry.getValue())
                    .findFirst()
                    .orElse(null);
        }
        if(field != null) {
            return true;
        }
        return false;
    }

    /**
     * Returns a map containg error messages for all the required fields that cannot
     * be found in one of log methods args, MDC or dataMap
     * @param allRequiredFields
     * @param args
     * @param dataMap
     * @param methodName
     * @return
     */
    private Map<String, String> checkAndReturnAllMissingRequiredFieldsErrors(Map<String, BiFunction<List<Object>,Map<String, Object>, Boolean>> allRequiredFields, List<Object> args, Map<String, Object> dataMap, String methodName) {
        Map<String, String> notFoundMap = new HashMap<>();
        if (allRequiredFields != null) {

            notFoundMap = allRequiredFields
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue().apply(args, dataMap) == false)
                    .collect(Collectors.toMap(entry -> entry.getKey(), entry -> constructFieldNotFoundErrorMsg(entry.getKey(), methodName)));
        }
        return notFoundMap;
    }

    /**
     * Returns true if all the required fields can be found in one of log methods args, MDC or dataMap
     * @param allRequiredFields
     * @param args
     * @param dataMap
     * @param methodName
     * @return
     */
    private boolean isAllRequiredFieldsFound(Map<String, BiFunction<List<Object>, Map<String, Object>, Boolean>> allRequiredFields, List<Object> args,  Map<String, Object> dataMap, String methodName){
        return checkAndReturnAllMissingRequiredFieldsErrors(allRequiredFields, args,dataMap, methodName).isEmpty();
    }

    /**
     * Constructs LogFieldNotFoundException message
     * @param field
     * @param methodName
     * @return
     */
    private String constructFieldNotFoundErrorMsg(String field, String methodName){
        return String.format(LOG_FIELD_NOT_FOUND_MSG, field, field,methodName, field);
    }

}
