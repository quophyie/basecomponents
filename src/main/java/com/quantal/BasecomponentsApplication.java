package com.quantal;

import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.logger.QuantalLogger;
import com.quantal.javashared.logger.QuantalLoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BasecomponentsApplication {

	public static void main(String[] args) throws Throwable {

		//Uncommment to run
		SpringApplication.run(BasecomponentsApplication.class, args);
		QuantalLogger logger = QuantalLoggerFactory.getLogger(BasecomponentsApplication.class, new CommonLogFields());

		logger.throwing(new NullPointerException());
		logger.with("StringTest1").with("StringTest2").info("Some string message");
	}
}
