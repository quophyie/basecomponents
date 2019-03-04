package com.quantal.javashared.logger;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.dto.LoggerConfig;
import com.quantal.javashared.dto.LogzioConfig;
import io.logz.sender.exceptions.LogzioParameterErrorException;
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
public class QuantalLoggerFactory {


    private static final int RECURSIVE_LEVEL = 100;
    public static QuantalLogger getLogger(Class<?> clazz, LoggerConfig loggerConfig) {
        if (loggerConfig.getGoDadayLoggerConfig() == null ){
            loggerConfig.setGoDadayLoggerConfig(LoggingConfigs.getCurrent());
            loggerConfig.setGoDadayLoggerConfig(loggerConfig.getGoDadayLoggerConfig().withRecursiveLevel(RECURSIVE_LEVEL));
        }
        Logger logger =   loggerConfig.getGoDadayLoggerConfig().getCurrent().getDefaultLogger(clazz);
        logger = standardizeLogLine(logger, loggerConfig.getCommonLogFields());
        QuantalLogger quantalGoDaddyLogger = null;
        try {
            quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, LoggingConfigs.getCurrent());
            quantalGoDaddyLogger.setCommonFields(loggerConfig.getCommonLogFields());
        } catch (LogzioParameterErrorException lpe) {
            throw new RuntimeException(lpe);
        }
        return quantalGoDaddyLogger;
    }
    //public static QuantalLogger getLogzioLogger(Class<?> clazz, CommonLogFields commonLogFields, LoggingConfigs configs, LogzioConfig logzioConfig) {
    public static QuantalLogger getLogzioLogger(Class<?> clazz, LoggerConfig loggerConfig) {

        if (loggerConfig.getGoDadayLoggerConfig() == null) {
            loggerConfig.setGoDadayLoggerConfig(LoggingConfigs.getCurrent());
            loggerConfig.setGoDadayLoggerConfig(loggerConfig.getGoDadayLoggerConfig().withRecursiveLevel(RECURSIVE_LEVEL));
        }
        Logger logger = loggerConfig.getGoDadayLoggerConfig().getConfiguredLogger(clazz, loggerConfig.getGoDadayLoggerConfig());
        logger = standardizeLogLine(logger, loggerConfig.getCommonLogFields());
        QuantalLogger quantalGoDaddyLogger = null;
        try {
            quantalGoDaddyLogger = new QuantalGoDaddyLoggerImpl(logger, loggerConfig);
            quantalGoDaddyLogger.setCommonFields(loggerConfig.getCommonLogFields());
        } catch (LogzioParameterErrorException lpe) {
            throw new RuntimeException(lpe);
        }
        return quantalGoDaddyLogger;
    }

    public static LogzioConfig createDefaultLogzioConfig(String logzioToken, Optional<Boolean> showDebugInfo, Optional<ScheduledExecutorService> tasksExecutor) {


        return new LogzioConfig(
                logzioToken,
                "javaSenderType",
                5,
                98,
                new File("logzio/buffer"),
                "https://listener.logz.io:8071",
                10 * 1000,
                10 * 1000,
                showDebugInfo.orElse(false),
                new LogzioStatusReporter(LoggerFactory.getLogger(LogzioStatusReporter.class)),
                tasksExecutor.orElse(Executors.newScheduledThreadPool(3)),
                30,
                true
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
