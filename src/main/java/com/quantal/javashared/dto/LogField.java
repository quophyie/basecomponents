package com.quantal.javashared.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by dman on 24/07/2017.
 */
@Data
@Builder
@NoArgsConstructor
public class LogField {
    private String key;
    private Object value;

    public LogField(String key, Object value){
        this.key = key;
        this.value = value;
    }
}
