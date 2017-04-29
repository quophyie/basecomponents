package com.quantal.shared.util;

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
     * in CompletableFuture chains where there is nested call to method that returns a completation
     * stage (CompletableFuture) which may have thrown an exception
     * NOTE: ****This method is meant to be used with a completion Stage (CompletableFuture)***
     * @param result - The result or exception thrown by the previous completion stage
     * @param <T> - The return type of the retult
     * @return
     */
    private <T> T getResultOrThrowException(Object result) {
        if (result instanceof CompletableFuture &&  ((CompletableFuture)result).isCompletedExceptionally()) {
            try {
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
    public static <T> T extractBusinessExceptionFromRuntimeException(Throwable throwable) {
        if (throwable.getCause().getClass() == java.util.concurrent.ExecutionException.class ||
                throwable.getCause().getClass() == java.lang.RuntimeException.class) {
            return extractBusinessExceptionFromRuntimeException(throwable.getCause());
        } else {
            return (T) throwable.getCause();
        }
    }
}
