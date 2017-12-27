package com.quantal;

import com.godaddy.logging.Logger;
import com.quantal.shared.dto.CommonLogFields;
import com.quantal.shared.logger.QuantalGoDaddyLogger;
import com.quantal.shared.logger.QuantalGoDaddyLoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BasecomponentsApplication {

	public static void main(String[] args) throws Throwable {

		//Uncommment to run
		SpringApplication.run(BasecomponentsApplication.class, args);
		QuantalGoDaddyLogger logger = QuantalGoDaddyLoggerFactory.getLogger(BasecomponentsApplication.class, new CommonLogFields());

		logger.throwing(new NullPointerException());
		logger.with("StringTest1").with("StringTest2").info("Some string message");
	}
}
