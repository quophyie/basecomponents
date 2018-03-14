package com.quantal.javashared.dto;

import com.quantal.javashared.constants.CommonConstants;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * Created by dman on 09/10/2017.
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class LogTraceId extends LogField{
    private String traceId;
    //private String msg;

    public LogTraceId(String traceId){
        this();
        super.setValue(traceId);
        this.traceId = traceId;
    }

    public LogTraceId(){
        super();
        super.setKey(CommonConstants.TRACE_ID_MDC_KEY);
    }

    @Override
    public void setKey(String key) {
        throw new IllegalArgumentException(String.format("TraceId key name cannot be changed. It will always remain as %s", CommonConstants.TRACE_ID_MDC_KEY));

    }
    @Override
    public void setValue (Object value){
        if (!(value instanceof String))
            throw new IllegalArgumentException("value must be a string");
        super.setValue(value);
        this.traceId = (String) value;

    }

    public static class LogTraceIdBuilder extends LogFieldBuilder{
        LogTraceIdBuilder(){
            super();
        }
    }
}
