package com.quantal.shared.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Created by dman on 29/04/2017.
 */
public class CommonUtils {

    /**
     * Returns the result  of a completion stage (ie. a completable future)
     * or throws an exception if the completion stage (CompletableFuture)
     * finished exceptionally
     * This method is best used as the outer handler method for handle or whenComplete methods
     * in CompletableFuture chains where there is nested call to method that returns a completion
     * stage (CompletableFuture) which may have thrown an exception
     * NOTE: ****This method is meant to be used with a completion Stage (CompletableFuture)***
     * @param result - The result or exception thrown by the previous completion stage
     * @param <T> - The return type of the retult
     * @return
     */
    public static <T> T getResultOrThrowException(Object result) {
        if ((result instanceof CompletableFuture &&  ((CompletableFuture)result).isCompletedExceptionally())
                || result instanceof Throwable) {
            try {
                if (result instanceof Throwable)
                    throw new RuntimeException(((Throwable)result).getCause());
                ((CompletableFuture)result).get();
            } catch (InterruptedException | ExecutionException e ) {
                throw new RuntimeException(e);
            }
        }

        return (T)result;
    }

    /**
     * Extracts A business Exception from an exception that is either of type
     * {@see java.util.concurrent.ExecutionException} or {@see java.lang.RuntimeException}
     * @param throwable  - the throwable to inspect
     * @param <T> the type param representing the type of the BusinessException
     * @return the business exception
     */
    public static <T> T extractBusinessException(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null) {
            if (cause.getClass() == java.util.concurrent.ExecutionException.class ||
                    cause.getClass() == java.lang.RuntimeException.class) {
                return extractBusinessException(throwable.getCause());
            } else {
                return (T) throwable.getCause();
            }
        }
        return (T)throwable;
    }

    /**
     * A generic handler to be passed to CompletableFuture.handle callback method
     * Returns the result  of a completion stage (ie. a completable future)
     * or throws an exception if the completion stage (CompletableFuture)
     * finished exceptionally
     * This method is best used as the outer handler method for handle method
     * in CompletableFuture chains where there is nested call to method that returns a completion
     * stage (CompletableFuture) which may have thrown an exception
     * NOTE: ****This method is meant to be used with a completion Stage (CompletableFuture)***
     * @param result
     * @param exception
     * @param <T>
     * @return
     */
    public static <T> T processHandle(T result, Throwable exception){
        Object res = result;
        if (exception != null) {
            res = exception;
        }
        return CommonUtils.getResultOrThrowException((res));
    }

    /**
     * A generic handler to be passed to CompletableFuture.whenComplete callback method
     * Returns the result  of a completion stage (ie. a completable future)
     * or throws an exception if the completion stage (CompletableFuture)
     * finished exceptionally
     * This method is best used as the outer handler method for handle method
     * in CompletableFuture chains where there is nested call to a method that returns a completion
     * stage (CompletableFuture) which may have thrown an exception
     * NOTE: ****This method is meant to be used with a completion Stage (CompletableFuture)***
     * @param result
     * @param exception
     * @param <T>
     */
    public static <T> void processWhenComplete(T result, Throwable exception){
         CommonUtils.processHandle(result, exception);
    }

    /**
     * Converts the given object to a Json string
     * @param object - The object to convert
     * @return
     * @throws IOException
     */
    public static String convertObjectToJsonStringUsingView(Object object, Class jsonView, ObjectMapper mapper) throws IOException {

        if (mapper == null) {
            mapper = new ObjectMapper();
        }
       // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        if (jsonView != null)
            return mapper.writerWithView(jsonView).writeValueAsString(object);
        return mapper.writeValueAsString(object);
    }

}
