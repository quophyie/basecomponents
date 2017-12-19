package com.quantal.shared.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class CommonLogFields {

        public CommonLogFields(){}
        //private String event;
        //private String msg;
        private String proglang;
        private String framework;
        private String frameworkVersion;
        private String name;
        private String hostname;
        private String moduleVersion;
        private String lang;
        private String time;
}
