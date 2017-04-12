package com.quantal.basecomponents.services.interfaces;

import java.util.Locale;

/**
 * Created by dman on 29/03/2017.
 */
public interface MessageService {

    String getMessage(String code);
    String getMessage(String code, Locale locale);
    public String getMessage(String code, String[] replacements);
    public String getMessage(String code, String[] replacements, Locale locale);
}
