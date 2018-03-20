package com.quantal.javashared.exceptions;

/**
 * Created by dman on 29/03/2017.
 */
public class HeaderNotFoundException extends RuntimeException {

    public HeaderNotFoundException(String message){
        super(message);
    }
}
