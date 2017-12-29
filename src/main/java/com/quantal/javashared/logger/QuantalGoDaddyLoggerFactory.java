package com.quantal.javashared.logger;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.dto.LogzioConfig;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by dman on 24/07/2017.
 */
public class QuantalGoDaddyLoggerFactory {


    public static QuantalGoDaddyLogger getLogger(Class<?> clazz, CommonLogFields commonLogFields) {
        Logger logger =  LoggingConfigs.getCurrent().getDefaultLogger(clazz);
        logger = standardizeLogLine(logger, commonLogFields);
        QuantalGoDaddyLogger quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, LoggingConfigs.getCurrent());
        quantalGoDaddyLogger.setCommoFields(commonLogFields);
        return quantalGoDaddyLogger;
    }

    public static QuantalGoDaddyLogger getLogger(Class<?> clazz, CommonLogFields commonLogFields, LoggingConfigs configs) {
        Logger logger =  configs.getConfiguredLogger(clazz, configs);
        logger = standardizeLogLine(logger, commonLogFields);
        QuantalGoDaddyLogger quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, configs);
        quantalGoDaddyLogger.setCommoFields(commonLogFields);
        return quantalGoDaddyLogger;
    }

    public static QuantalGoDaddyLogger getLogzioLogger(Class<?> clazz, CommonLogFields commonLogFields, LoggingConfigs configs, LogzioConfig logzioConfig) {
        Logger logger =  configs.getConfiguredLogger(clazz, configs);
        logger = standardizeLogLine(logger, commonLogFields);
        QuantalGoDaddyLogger quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, configs, logzioConfig);
        quantalGoDaddyLogger.setCommoFields(commonLogFields);
        return quantalGoDaddyLogger;
    }

    public static QuantalGoDaddyLogger getLogzioLogger(Class<?> clazz, CommonLogFields commonLogFields, LogzioConfig logzioConfig) {

        return getLogzioLogger(clazz, commonLogFields, LoggingConfigs.getCurrent(), logzioConfig);
    }

    public static LogzioConfig createDefaultLogzioConfig(String logzioToken, Optional<Boolean> showDebugInfo, Optional<ScheduledExecutorService> tasksExecutor){
        return new LogzioConfig(
                logzioToken,
                "java",
                5,
                98,
                new File("logio/buffer"),
                "https://listener.logz.io:8071",
                10 * 1000,
                10 * 1000,
                showDebugInfo.orElse(false),
                new LogzioStatusReporter(LoggerFactory.getLogger(QuantalGoDaddyLogger.class)),
                tasksExecutor.orElse(Executors.newScheduledThreadPool(2)),
                30
        );
    }


    private static Logger standardizeLogLine(Logger logger, CommonLogFields logLineEvent){
        Logger[] finalLogger = new Logger[]{logger};
        Arrays.asList(ReflectionUtils.getAllDeclaredMethods(logLineEvent.getClass()))
                .stream()
                .filter(method -> method.getName().startsWith("get") &&  !method.getName().equalsIgnoreCase("getClass"))
                .forEach(method -> {
                    try {
                        String key = method.getName().substring(3).toLowerCase();
                        Object value = method.invoke(logLineEvent);
                        finalLogger[0] = finalLogger[0].with(key, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        logger.error(e.getMessage(), e );
                    }
                });
        return  finalLogger[0];

    }
}
