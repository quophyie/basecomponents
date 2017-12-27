package com.quantal.shared.controller;

import com.quantal.shared.dto.CommonLogFields;
import com.quantal.shared.dto.LogEvent;
import com.quantal.shared.dto.ResponseDto;
import com.quantal.shared.logger.QuantalGoDaddyLogger;
import com.quantal.shared.logger.QuantalGoDaddyLoggerFactory;
import io.logz.sender.exceptions.LogzioParameterErrorException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public final class TestController extends BaseControllerAsync{

    private QuantalGoDaddyLogger logger = QuantalGoDaddyLoggerFactory.getLogger(this.getClass(), new CommonLogFields());

    public TestController() throws LogzioParameterErrorException {
    }

    @RequestMapping("/")
    public String get(){

        logger.info("Testing log {}","the param", new LogEvent("TEST_EVENT"), new ResponseDto("Amessage",300, "SomeString"));
        return "GET success";
    }
}
