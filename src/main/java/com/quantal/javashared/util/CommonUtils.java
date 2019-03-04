package com.quantal.javashared.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantal.javashared.dto.LogField;
import com.quantal.javashared.dto.ThreadDetails;
import com.quantal.javashared.filters.EventAndTraceIdMdcPopulatingFilter;
import org.slf4j.MDC;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.quantal.javashared.constants.CommonConstants.EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.TRACE_ID_MDC_KEY;

/**
 * Created by dman on 29/04/2017.
 */
public class CommonUtils {
    public static final String REST_ENDPOINT_PATTERN = "((http|https)://){0,1}[a-zA-Z0-9.]*([:]{1}\\d{1,6}){0,1}%s[/]{0,1}[A-Za-z0-9/.]*";
    public static final String HOST_PATTERN = "((http|https)://){0,1}[a-zA-Z0-9.]*([:]{1}\\d{1,6}){0,1}";

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
     * or {java.util.concurrent.CompletionException.class} or {java.util.concurrent.CancellationException.class}
     * @param throwable  - the throwable to inspect
     * @param <T> the type param representing the type of the BusinessException
     * @return the business exception
     */
    public static <T> T extractBusinessException(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause != null) {
            if (cause.getClass() == java.util.concurrent.ExecutionException.class ||
                    cause.getClass() == java.lang.RuntimeException.class
                    || cause.getClass() == java.util.concurrent.CompletionException.class
                    || cause.getClass() == java.util.concurrent.CancellationException.class) {
                return extractBusinessException(throwable.getCause());
            } else {
                return (T) throwable.getCause();
            }
        }
        return (T)throwable;
    }

    /**
     * Extracts A business Exception from an exception that is either of type
     * {@see java.util.concurrent.ExecutionException} or {@see java.lang.RuntimeException}
     * or {java.util.concurrent.CompletionException.class} or {java.util.concurrent.CancellationException.class}
     * checked exception and returns the Exception as RuntimeException
     * @param throwable  - the throwable to inspect
     * @return the business exception
     */
    public static RuntimeException extractBusinessExceptionAsRuntimeException(Throwable throwable) {
        Throwable cause =  extractBusinessException(throwable.getCause());

        if (!(cause instanceof RuntimeException)){
           cause = new RuntimeException(cause.getMessage(), cause);
        }

        return  (RuntimeException) cause;

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

    /**
     * This method determines whether a call to the supplied endpoint will require the mandatory propagated headers (by default X-EVENT
     * and X-TraceId).
     * Returns true if the supplied endpoint is not a match for any of the regex patterns in the list
     * of serviceEndpointsNotRequiringMandatoryPropagatedHeadersPatterns list.
     * A no match will mean that the call to the supplied endpoint will  require the mandatory headers
     *
     * @param endpoint - The endpoint that the will be tested against the patterns in the serviceEndpointsNotRequiringMandatoryPropagatedHeadersPatterns list
     *                 to determine whether it will require the mandatory headers propagated to other microservices
     *
     *
     * @param serviceEndpointsNotRequiringMandatoryPropagatedHeadersPatterns - A list containing endpoints regex patterns which the endpoint
     *                                                                      will be tested against to determine if there is a match or not
     *
     *
     *
     * @return true if the supplied endpoint requires the mandatory propagated headers (by default X-EVENT and X-TraceId), and false otherwise
     */
    public static boolean isMandatoryPropagatedHeadersRequiredToCallEndpoint(final String endpoint,
                                                                             final List<String> serviceEndpointsNotRequiringMandatoryPropagatedHeadersPatterns
    ){


        if (serviceEndpointsNotRequiringMandatoryPropagatedHeadersPatterns != null){
            long numServiceEndpointsRequiringMandatoryPropagatedHeaders =
                    serviceEndpointsNotRequiringMandatoryPropagatedHeadersPatterns
                            .stream()
                            .map(nonPropReqHeadPatt -> nonPropReqHeadPatt.replace(HOST_PATTERN, ""))
                            .map(nonPropReqHeadPatt -> String.format(REST_ENDPOINT_PATTERN, nonPropReqHeadPatt))
                            .map(Pattern::compile)
                            .flatMap(compiledPattern -> Stream.of(new AbstractMap.SimpleEntry(endpoint, compiledPattern)))
                            .filter(simpleEntry ->  isEndpointNotMatched((String) simpleEntry.getKey(), (Pattern) simpleEntry.getValue()))
                            .count();

            return numServiceEndpointsRequiringMandatoryPropagatedHeaders > 0;
        }

        return false;
    }

    /**
     * Will update the MDC with the traceId and event if they are missing from the MDC
     * The traceId and event are usually missing when the a CompletionStageException (i.e. CompletableFutureException)
     * is handled on a different thread other than the calling thread i.e. the thread that generated the exception
     * @param ex
     */
    public static void tryUpdateMDCWithEventAndTraceIdIfMissingFromMDC(final Throwable ex,
                                                              final EventAndTraceIdMdcPopulatingFilter eventAndTraceIdMdcPopulatingFilter){

        if(MDC.get(TRACE_ID_MDC_KEY) == null) {
            String traceId = eventAndTraceIdMdcPopulatingFilter.getEventAndTraceIdMap().get().get(TRACE_ID_MDC_KEY);
            MDC.put(TRACE_ID_MDC_KEY, traceId);
        }

        if (MDC.get(EVENT_KEY) == null) {
            String event;
            if (ex.getCause() != null){
                event = ex.getCause().getClass().getSimpleName().toUpperCase();

            } else {
                event = ex.getClass().getSimpleName().toUpperCase();
            }
            MDC.put(EVENT_KEY, event);
        }
    }

    public static <T> DeferredResult<T> from(final CompletableFuture<T> future) {
        final DeferredResult<T> deferred = new DeferredResult<>();
        future.thenAccept(deferred::setResult);
        future.exceptionally(ex -> {
            if (ex instanceof CompletionException) {
                deferred.setErrorResult(ex.getCause());
            } else {
                deferred.setErrorResult(ex);
            }
            return null;
        });
        return deferred;
    }


    private static boolean isEndpointNotMatched(String endpoint, Pattern pattern){

        if (pattern == null || endpoint == null) {
            return true;
        }
        return !pattern.matcher(endpoint).matches();
    }

}
