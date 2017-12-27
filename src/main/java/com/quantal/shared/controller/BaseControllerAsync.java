package com.quantal.shared.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantal.shared.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by dman on 02/05/2017.
 */
public abstract class BaseControllerAsync {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    protected <T> ResponseEntity applyJsonView(ResponseEntity entity, Class jsonView, ObjectMapper mapper) {

        ResponseEntity newResponseEntity = null;
        try {
            T entityBody = (T)entity.getBody();
            String jsonString = null;
            jsonString = CommonUtils.convertObjectToJsonStringUsingView(entityBody, jsonView, mapper);
             newResponseEntity = new ResponseEntity(jsonString, entity.getHeaders(), entity.getStatusCode());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return newResponseEntity;
    }

    protected <T> CompletableFuture<ResponseEntity> applyJsonViewAsync(ResponseEntity entity, Class jsonView, ObjectMapper mapper) {

        ResponseEntity newResponseEntity = applyJsonView(entity, jsonView, mapper);

        return CompletableFuture.completedFuture(newResponseEntity);
    }


}
