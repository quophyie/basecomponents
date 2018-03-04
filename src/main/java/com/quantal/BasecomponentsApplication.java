package com.quantal;

import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.logger.QuantalLogger;
import com.quantal.javashared.logger.QuantalLoggerFactory;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Optional;

@SpringBootApplication
public class BasecomponentsApplication {

	public static void main(String[] args) throws Throwable {

		//Uncommment to run
		//SpringApplication.run(BasecomponentsApplication.class, args);
		CommonLogFields commonLogFields =  new CommonLogFields();
		commonLogFields.setFrameworkVersion(new String("1.0.1"));
		commonLogFields.setFramework(1.00);
		QuantalLogger logger = QuantalLoggerFactory.getLogzioLogger(BasecomponentsApplication.class,commonLogFields, QuantalLoggerFactory.createDefaultLogzioConfig("xfYVZUGzUaWAMPjUyUaBsVlpUszcuCaY", Optional.of(true), Optional.empty()));

		logger.throwing(new NullPointerException());
		logger.error(new ObjectAppendingMarker("TestMarker", "testMarkerFieldName"), "test markerMsg", new NullPointerException());
		logger.with("StringTest1").with("StringTest2").info("Some string message");
		logger.with("StringTest1").with("StringTest2").info("Some string message");
		logger.with("String1","StringTest1").with("String2","StringTest2").info("Some string message 1");
		logger.with("String3","StringTest3").with("String4","StringTest4").info("Some string message 2");

	}
}
