package com.quantal.javashared.filters;

import com.google.common.collect.ImmutableMap;
import com.quantal.javashared.annotations.logger.InjectLogger;
import com.quantal.javashared.constants.Events;
import com.quantal.javashared.dto.LogEvent;
import com.quantal.javashared.logger.QuantalLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static com.quantal.javashared.constants.CommonConstants.EVENT_HEADER_KEY;
import static com.quantal.javashared.constants.CommonConstants.EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.REQUEST_KEY;
import static com.quantal.javashared.constants.CommonConstants.RESPONSE_KEY;
import static com.quantal.javashared.constants.CommonConstants.SUB_EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.TRACE_ID_HEADER_KEY;
import static com.quantal.javashared.constants.CommonConstants.TRACE_ID_MDC_KEY;
import static com.quantal.javashared.constants.Events.DEFAULT_REQUEST_EVENT;

/**
 * Created by dman on 08/07/2017.
 */


//@Component
//@Configurable
public class EventAndTraceIdMdcPopulatingFilter extends OncePerRequestFilter {


    /**
     * Populates Mdc with event and traceId
     */

    @InjectLogger
    private QuantalLogger logger;

    public String generateTraceId(){
        return  UUID.randomUUID().toString();
    }

    private static ThreadLocal<ImmutableMap<String, String>> traceIdMap = null;

    @Override
    //public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        resetMDCAndThreadContext();
        try {

            String traceId = !StringUtils.isEmpty(((HttpServletRequest) request).getHeader(TRACE_ID_HEADER_KEY)) ? ((HttpServletRequest) request).getHeader(TRACE_ID_HEADER_KEY) : generateTraceId();
            String event = !StringUtils.isEmpty(((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY)) ? ((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY) : DEFAULT_REQUEST_EVENT;
            String requestUri = String.format("uri: %s, remoteAddr:%s ,remoteHost:remotePort: %s:%s", ((HttpServletRequest) request).getRequestURI(),
                    request.getRemoteAddr(), request.getRemoteHost(), request.getRemotePort());

            String msg = String.format("request received from %s", requestUri);
            if(!StringUtils.isEmpty(((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY))){
                msg = String.format("progressing %", ((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY));
            }


            MDC.put(TRACE_ID_MDC_KEY, traceId);
            MDC.put(EVENT_KEY, event);
            String finalTraceId = traceId;
            String finalEvent = event;
            traceIdMap = ThreadLocal.withInitial(() ->
                    ImmutableMap.of(TRACE_ID_MDC_KEY, finalTraceId, EVENT_KEY, finalEvent));
            //traceIdMap = traceIdMap.of(TRACE_ID_MDC_KEY, traceId);

            logger.with(REQUEST_KEY, request.toString())
                    .with(EVENT_KEY, event)
                    .info(msg);
            chain.doFilter(request, response);
            traceId = ((HttpServletResponse) response).getHeader(TRACE_ID_HEADER_KEY);
            if (StringUtils.isEmpty(traceId)) {
                ((HttpServletResponse) response).addHeader(TRACE_ID_HEADER_KEY, ThreadContext.get(TRACE_ID_MDC_KEY));
            }

            if (MDC.get(EVENT_KEY).equalsIgnoreCase(DEFAULT_REQUEST_EVENT)){
                MDC.remove(SUB_EVENT_KEY);
            }

            logger.with(RESPONSE_KEY, response.toString()).debug("response sent successfully ", new LogEvent(Events.DEFAULT_RESPONSE_EVENT));
        } finally {

            //resetMDCAndThreadContext();
        }
    }


    public ThreadLocal<ImmutableMap<String, String>> getEventAndTraceIdMap(){
        return traceIdMap;
    }
    @Override
    public void destroy() {
        logger.with(EVENT_KEY, "FILTER_DESTROY")
                .with(TRACE_ID_MDC_KEY, generateTraceId())
                .warn("Destroying EventAndTraceIdMdcPopulatingFilter ...");
    }

    private void resetMDCAndThreadContext(){
        ThreadContext.remove(TRACE_ID_MDC_KEY);
        MDC.remove(TRACE_ID_HEADER_KEY);
        ThreadContext.remove(EVENT_KEY);
        MDC.remove(EVENT_KEY);
        MDC.remove(SUB_EVENT_KEY);
        ThreadContext.remove(SUB_EVENT_KEY);
    }

    //@Override
    //protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

    //}
}
