package com.quantal.javashared.dto;

import com.godaddy.logging.LoggingConfigs;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

@Data
@Builder
@AllArgsConstructor
public class LoggerConfig {

    private LogzioConfig logzioConfig;
    private LoggingConfigs goDadayLoggerConfig;
    @Builder.Default
    private boolean eventIsRequired = true;
    @Builder.Default
    private boolean traceIdIsRequired = true;
    //Key is the name of fields to find /check for, the bifunction is the function
    //that is used find the key in the MDC or log statement  args object array / map supplied as arguments
    // to the bifunction.
    //Will return true if the key is found in one of MDC, log arguments or the map supplied as arg to BiFunction
    @Builder.Default
    private Map<String , BiFunction<List<Object>, Map<String, Object>, Boolean>> requiredLogFields = new HashMap<>();
    private CommonLogFields commonLogFields;
    //If true, the required fields which were not supplied in the MDC, log method args or via a`with` method
    // will be added as one of the ELK/logz.io fields but their values will be set to null
    @Builder.Default
    private boolean addMissingFieldsToElkLogs= true;

    //If true, the thread details of the currently executing such as the threadId, threadName and thread group will be added
    @Builder.Default
    private boolean includeThreadDetails= true;
    public LoggerConfig(){

    }
}
