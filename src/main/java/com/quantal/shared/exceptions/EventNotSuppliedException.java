package com.quantal.shared.exceptions;

/**
 * Created by dman on 29/03/2017.
 */
public class EventNotSuppliedException extends RuntimeException {

    public EventNotSuppliedException(){}
    public EventNotSuppliedException(String message){
        super(message);
    }
}
