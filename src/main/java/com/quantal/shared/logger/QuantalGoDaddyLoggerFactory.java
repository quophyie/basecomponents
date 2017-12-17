package com.quantal.shared.logger;

import com.godaddy.logging.Logger;
import com.godaddy.logging.LoggingConfigs;
import com.quantal.shared.dto.CommonLogFields;
import com.quantal.shared.dto.LogEvent;

/**
 * Created by dman on 24/07/2017.
 */
public class QuantalGoDaddyLoggerFactory {


    /**
     *     private String proglang;
     private String framework;
     private String frameworkVersion;
     private String name;
     private String hostname;
     private String moduleVersion;
     private String lang;
     private String time;
     * @param clazz
     * @param commonLogFields
     * @return
     */
    public static Logger getLogger(Class<?> clazz, CommonLogFields commonLogFields) {
        Logger logger =  LoggingConfigs.getCurrent().getDefaultLogger(clazz);
        return standardizeLogLine(logger, commonLogFields);
    }

    public static Logger getLogger(Class<?> clazz, CommonLogFields commonLogFields, LoggingConfigs configs) {
        Logger logger =  configs.getConfiguredLogger(clazz, configs);
        return standardizeLogLine(logger, commonLogFields);
    }

    public static Logger getLogger(Class<?> clazz) {
        return LoggingConfigs.getCurrent().getDefaultLogger(clazz);
    }

    public static Logger getLogger(Class<?> clazz, LoggingConfigs configs) {
        return configs.getConfiguredLogger(clazz, configs);
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
