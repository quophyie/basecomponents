package com.quantal.shared.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantal.shared.util.CommonUtils;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Created by dman on 02/05/2017.
 */
public abstract class BaseAsyncController {

    protected <T> ResponseEntity applyJsonViewA(ResponseEntity entity, Class jsonView, ObjectMapper mapper) throws IOException {

        T entityBody = (T)entity.getBody();
        String jsonString = CommonUtils.convertObjectToJsonStringUsingView(entityBody, jsonView, mapper);
        ResponseEntity newResponseEntity = new ResponseEntity(jsonString, entity.getHeaders(), entity.getStatusCode());

        return newResponseEntity;
    }

    protected <T> CompletableFuture<ResponseEntity> applyJsonViewAsync(ResponseEntity entity, Class jsonView, ObjectMapper mapper) throws IOException {

        T entityBody = (T)entity.getBody();
        String jsonString = CommonUtils.convertObjectToJsonStringUsingView(entityBody, jsonView, mapper);
        ResponseEntity newResponseEntity = new ResponseEntity(jsonString, entity.getHeaders(), entity.getStatusCode());

        return CompletableFuture.completedFuture(newResponseEntity);
    }


}
