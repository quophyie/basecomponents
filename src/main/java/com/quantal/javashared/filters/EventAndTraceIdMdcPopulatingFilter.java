package com.quantal.javashared.filters;

import com.quantal.javashared.annotations.logger.InjectLogger;
import com.quantal.javashared.dto.LogEvent;
import com.quantal.javashared.logger.QuantalLogger;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.web.filter.GenericFilterBean;

import static com.quantal.javashared.constants.CommonConstants.EVENT_HEADER_KEY;
import static com.quantal.javashared.constants.CommonConstants.EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.REQUEST_KEY;
import static com.quantal.javashared.constants.CommonConstants.RESPONSE_KEY;
import static com.quantal.javashared.constants.CommonConstants.TRACE_ID_HEADER_KEY;
import static com.quantal.javashared.constants.CommonConstants.TRACE_ID_MDC_KEY;

/**
 * Created by dman on 08/07/2017.
 */


//@Component
//@Configurable
public class EventAndTraceIdMdcPopulatingFilter extends GenericFilterBean {


    /**
     * Populates Mdc with event and traceId
     */

    @InjectLogger
    private QuantalLogger logger;

    public String generateTraceId(){
        return  UUID.randomUUID().toString();
    }
    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {

        try {
            String traceId = !StringUtils.isEmpty(((HttpServletRequest) request).getHeader(TRACE_ID_HEADER_KEY)) ? ((HttpServletRequest) request).getHeader(TRACE_ID_HEADER_KEY) : generateTraceId();
            String event = !StringUtils.isEmpty(((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY)) ? ((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY) : "REQUEST_RECEIVED";

            String msg = "request received successfully";
            if(!StringUtils.isEmpty(((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY))){
                msg = String.format("progressing %", ((HttpServletRequest) request).getHeader(EVENT_HEADER_KEY));
            }


            MDC.put(TRACE_ID_MDC_KEY, traceId);
            MDC.put(EVENT_HEADER_KEY, event);

            logger.with(REQUEST_KEY, request.toString())
                    .with(EVENT_KEY, event)
                    .info(msg);
            chain.doFilter(request, response);
            traceId = ((HttpServletResponse) response).getHeader(TRACE_ID_HEADER_KEY);
            if (StringUtils.isEmpty(traceId)) {
                ((HttpServletResponse) response).addHeader(TRACE_ID_HEADER_KEY, ThreadContext.get(TRACE_ID_MDC_KEY));
            }

            logger.with(RESPONSE_KEY, response.toString()).debug("response sent successfully ", new LogEvent("RESPONSE_SENT"));
        } finally {
            ThreadContext.remove(TRACE_ID_MDC_KEY);
            MDC.remove(TRACE_ID_HEADER_KEY);
            ThreadContext.remove(EVENT_KEY);
            MDC.remove(EVENT_HEADER_KEY);
        }
    }

    @Override
    public void destroy() {
        logger.with(EVENT_KEY, "FILTER_DESTROY")
                .with(TRACE_ID_MDC_KEY, generateTraceId())
                .warn("Destroying EventAndTraceIdMdcPopulatingFilter ...");
    }
}
