package com.quantal.javashared.exceptions;

/**
 * Created by dman on 29/03/2017.
 */
public class LogFieldNotSuppliedException extends RuntimeException {

    public LogFieldNotSuppliedException(){}
    public LogFieldNotSuppliedException(String message){
        super(message);
    }
}
