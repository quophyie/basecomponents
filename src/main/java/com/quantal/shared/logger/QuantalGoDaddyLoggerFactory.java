package com.quantal.shared.logger;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.quantal.shared.dto.CommonLogFields;
import com.quantal.shared.dto.LogzioConfig;
import io.logz.sender.exceptions.LogzioParameterErrorException;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Executors;

/**
 * Created by dman on 24/07/2017.
 */
public class QuantalGoDaddyLoggerFactory {


    public static QuantalGoDaddyLogger getLogger(Class<?> clazz, CommonLogFields commonLogFields) throws LogzioParameterErrorException {
        Logger logger =  LoggingConfigs.getCurrent().getDefaultLogger(clazz);
        logger = standardizeLogLine(logger, commonLogFields);
        QuantalGoDaddyLogger quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, LoggingConfigs.getCurrent());
        quantalGoDaddyLogger.setCommoFields(commonLogFields);
        return quantalGoDaddyLogger;
    }

    public static QuantalGoDaddyLogger getLogger(Class<?> clazz, CommonLogFields commonLogFields, LoggingConfigs configs) throws LogzioParameterErrorException {
        Logger logger =  configs.getConfiguredLogger(clazz, configs);
        logger = standardizeLogLine(logger, commonLogFields);
        QuantalGoDaddyLogger quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, LoggingConfigs.getCurrent());
        quantalGoDaddyLogger.setCommoFields(commonLogFields);
        return quantalGoDaddyLogger;
    }

    public static LogzioConfig createDefaultLogzioConfig(String logzioToken){
        return new LogzioConfig(
                logzioToken,
                "java",
                5,
                98,
                new File("logio/buffer"),
                "https://listener.logz.io:8071",
                10 * 1000,
                10 * 1000,
                true,
                new LogzioStatusReporter(LoggerFactory.getLogger(QuantalGoDaddyLogger.class)),
                Executors.newScheduledThreadPool(2),
                30
        );
    }


    private static Logger standardizeLogLine(Logger logger, CommonLogFields logLineEvent){
        return logger
                .with("proglang", logLineEvent.getProglang())
                .with("framework", logLineEvent.getFramework())
                .with("frameworkVersion", logLineEvent.getFrameworkVersion())
                .with("name", logLineEvent.getName())
                .with("hostname", logLineEvent.getHostname())
                .with("moduleVersion", logLineEvent.getModuleVersion())
                .with("lang", logLineEvent.getLang())
                .with("time", logLineEvent.getTime());

    }
}
