package com.quantal.javashared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CommonLogFields {

        public CommonLogFields(){}
        //private String event;
        //private String msg;
        private Object proglang;
        private Object framework;
        private Object frameworkVersion;
        private Object name;
        private Object hostname;
        private Object moduleVersion;
        private Object lang;
        private Object time;
}
