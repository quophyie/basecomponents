package com.quantal.javashared.controller;

import com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders;
import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.dto.LogEvent;
import com.quantal.javashared.dto.LogTraceId;
import com.quantal.javashared.dto.LoggerConfig;
import com.quantal.javashared.dto.ResponseDto;
import com.quantal.javashared.logger.QuantalLogger;
import com.quantal.javashared.logger.QuantalLoggerFactory;
import io.logz.sender.exceptions.LogzioParameterErrorException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
//@EnforceRequiredHeaders
public class TestController extends BaseControllerAsync{


    private QuantalLogger logger = QuantalLoggerFactory.getLogger(this.getClass(), LoggerConfig.builder().commonLogFields( new CommonLogFields()).build());

    public TestController() throws LogzioParameterErrorException {
    }

    @GetMapping("/")
    public String get(){

        logger.info("Testing log {}","the param", new LogEvent("TEST_EVENT"), new LogTraceId(String.valueOf(System.currentTimeMillis())), new ResponseDto("Amessage",300, "SomeString"));
        return "GET success";
    }


    @EnforceRequiredHeaders
    @GetMapping({"/requires-header", "/requires-header/"})
    public String requiresHeader(){

        logger.info("Testing {} annotation",EnforceRequiredHeaders.class, new LogEvent("TEST_EVENT"), new LogTraceId(String.valueOf(System.currentTimeMillis())), new ResponseDto("Amessage",300, "SomeString"));
        return "GET requiresheader";
    }
}
