package com.quantal.javashared.logger;

/**
 * Created by dman on 24/07/2017.
 */
public class LoggerFactory {

    public static QuantalJsonLogger getLogger(Class clazz){
        return new QuantalJsonLogger(clazz);
    }

    public static QuantalJsonLogger getLogger(String name){
        return new QuantalJsonLogger(name);
    }
}
