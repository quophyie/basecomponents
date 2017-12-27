package com.quantal.javashared.services.implementations;

import com.quantal.javashared.services.interfaces.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;

import java.util.Locale;

/**
 * Created by dman on 29/03/2017.
 */
public class MessageServiceImpl implements MessageService {

    MessageSource messageSource;

    @Autowired
    public MessageServiceImpl(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String code) {
        return messageSource.getMessage(code, null, null);
    }

    @Override
    public String getMessage(String code, Locale locale) {
        return messageSource.getMessage(code, null, locale);
    }

    @Override
    public String getMessage(String code, String[] replacements) {
        return messageSource.getMessage(code, replacements, null);
    }

    @Override
    public String getMessage(String code, String[] replacements, Locale locale) {
        return messageSource.getMessage(code, replacements, locale);
    }
}
