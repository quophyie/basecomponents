package com.quantal;

import com.quantal.javashared.aspects.RetrofitRequiredHeadersEnforcerAspect;
import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.dto.LogEvent;
import com.quantal.javashared.dto.LogField;
import com.quantal.javashared.dto.LogTraceId;
import com.quantal.javashared.dto.LoggerConfig;
import com.quantal.javashared.exceptions.LogFieldNotSuppliedException;
import com.quantal.javashared.logger.QuantalLogger;
import com.quantal.javashared.logger.QuantalLoggerFactory;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.slf4j.MDC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static com.quantal.javashared.constants.CommonConstants.EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.SUB_EVENT_KEY;
import static com.quantal.javashared.constants.CommonConstants.TRACE_ID_MDC_KEY;

//@SpringBootApplication
@EnableAspectJAutoProxy
public class BasecomponentsApplication {

	@Bean
	public RetrofitRequiredHeadersEnforcerAspect requestHeadersAspect(){
		RetrofitRequiredHeadersEnforcerAspect requestHeadersAspect = new RetrofitRequiredHeadersEnforcerAspect();
		return  requestHeadersAspect;

	}
	public static void main(String[] args) throws Throwable {

		//Uncommment to run
		SpringApplication.run(BasecomponentsApplication.class, args);

		CommonLogFields commonLogFields =  new CommonLogFields();
		commonLogFields.setFrameworkVersion(new String("1.0.1"));
		commonLogFields.setFramework(1.00);
		LoggerConfig loggerConfig = LoggerConfig.builder()

												.commonLogFields(commonLogFields)
				  								.logzioConfig(QuantalLoggerFactory.createDefaultLogzioConfig("xfYVZUGzUaWAMPjUyUaBsVlpUszcuCaY", Optional.of(true), Optional.empty()))
												.build();
		QuantalLogger logger = QuantalLoggerFactory.getLogzioLogger(BasecomponentsApplication.class, loggerConfig);
		Exception nullPointerEx = new NullPointerException("Some NullPointerException");
		logger.throwing(nullPointerEx, new LogEvent("EXCEPTION_EVENT"), new LogField(SUB_EVENT_KEY, String.format("SOME_EX_SUBEVENT %s", nullPointerEx.getMessage())), new LogTraceId("TEST_EX_TRACE_ID"));
		logger.error(new ObjectAppendingMarker("TestMarker", "testMarkerFieldName"), "test markerMsg", new NullPointerException(), new LogTraceId("TEST_TRACE_ID"), new LogEvent("TEST_EVENT"));
		logger.with(new LogTraceId("TEST_TRACE_ID")).with(new LogEvent("TEST_EVENT")).with("StringTest1").with("StringTest2").info("Some string message");
		logger.with(new LogTraceId("TEST_TRACE_ID")).with(new LogEvent("TEST_EVENT")).with("StringTest1").with("StringTest2").info("Some string message");
		logger.with(new LogTraceId("TEST_TRACE_ID")).with(new LogEvent("TEST_EVENT")).with("String1","StringTest1").with("String2","StringTest2").info("Some string message 1");
		logger.with(new LogTraceId("TEST_TRACE_ID")).with(new LogEvent("TEST_EVENT")).with("String3","StringTest3").with("String4","StringTest4").info("Some string message 2");
        logger.info("There should be no LogFieldNotSuppliedException thrown as both event and traceId Fields supplied in args", new LogTraceId("TEST_TRACE_ID"), new LogEvent("TEST_EVENT"));

        MDC.put(EVENT_KEY, "EVENT_IN_MDC_AS_STRING");
        MDC.put(TRACE_ID_MDC_KEY, "TRACE_ID_IN_MDC_AS_STRING");
        logger.info("There should be NO LogFieldNotSuppliedException thrown as both event and traceId Fields supplied VIA MDC as Strings");
        logger.info("The following log lines should THROW LogFieldNotSuppliedException as event and traceId not supplied");
        MDC.setContextMap( new HashMap<>());

		try {
			logger.info("Should throw exception as event and traceId not supplied");
		} catch (LogFieldNotSuppliedException lfnse){
			System.out.println(lfnse);
		}


        try {
            logger.info("Should throw LogFieldNotSuppliedException as event not supplied", new LogTraceId("TEST_TRACE_ID"));
        } catch (LogFieldNotSuppliedException lfnse){
            System.out.println(lfnse);
        }

        try {
            logger.info("Should throw LogFieldNotSuppliedException as traceId not supplied", new LogEvent("TEST_EVENT"));
        } catch (LogFieldNotSuppliedException lfnse){
            System.out.println(lfnse);
        }

	}
}
