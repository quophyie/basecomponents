package com.quantal.javashared.controller;

import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.dto.LogEvent;
import com.quantal.javashared.dto.ResponseDto;
import com.quantal.javashared.logger.QuantalLogger;
import com.quantal.javashared.logger.QuantalLoggerFactory;
import io.logz.sender.exceptions.LogzioParameterErrorException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public final class TestController extends BaseControllerAsync{

    private QuantalLogger logger = QuantalLoggerFactory.getLogger(this.getClass(), new CommonLogFields());

    public TestController() throws LogzioParameterErrorException {
    }

    @RequestMapping("/")
    public String get(){

        logger.info("Testing log {}","the param", new LogEvent("TEST_EVENT"), new ResponseDto("Amessage",300, "SomeString"));
        return "GET success";
    }
}
