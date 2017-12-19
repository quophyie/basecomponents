package com.quantal.shared.logger;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.quantal.shared.dto.CommonLogFields;

/**
 * Created by dman on 24/07/2017.
 */
public class QuantalGoDaddyLoggerFactory {


    public static QuantalGoDaddyLogger getLogger(Class<?> clazz, CommonLogFields commonLogFields) {
        Logger logger =  LoggingConfigs.getCurrent().getDefaultLogger(clazz);
        logger = standardizeLogLine(logger, commonLogFields);
        QuantalGoDaddyLogger quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, LoggingConfigs.getCurrent());
        return quantalGoDaddyLogger;
    }

    public static QuantalGoDaddyLogger getLogger(Class<?> clazz, CommonLogFields commonLogFields, LoggingConfigs configs) {
        Logger logger =  configs.getConfiguredLogger(clazz, configs);
        logger = standardizeLogLine(logger, commonLogFields);
        QuantalGoDaddyLogger quantalGoDaddyLogger= new QuantalGoDaddyLoggerImpl(logger, LoggingConfigs.getCurrent());
        return quantalGoDaddyLogger;
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
