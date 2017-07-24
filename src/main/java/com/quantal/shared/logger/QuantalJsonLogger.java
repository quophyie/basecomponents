package com.quantal.shared.logger;

import com.savoirtech.logging.slf4j.json.logger.JsonLogger;
import org.slf4j.ext.XLogger;
import com.savoirtech.logging.slf4j.json.LoggerFactory;
import org.slf4j.ext.XLoggerFactory;

import java.util.List;
import java.util.Map;


/**
 * Created by dman on 19/07/2017.
 */
public class QuantalJsonLogger extends XLogger {

    private  com.savoirtech.logging.slf4j.json.logger.Logger logger;

    public QuantalJsonLogger(Class clazz) {
        super(XLoggerFactory.getXLogger(clazz));
        this.logger =  LoggerFactory.getLogger(clazz);
    }

    public QuantalJsonLogger(String name) {
        super(XLoggerFactory.getXLogger(name));
        this.logger =  LoggerFactory.getLogger(name);
    }

    @Override
    public void trace(String message, Object... args){
        JsonLogger log = logger.trace()
                .message(message);
        toJson(log, args);
    }

    @Override
    public void trace(String message){
        JsonLogger log = logger.trace()
                .message(message);
        toJson(log);
    }

    @Override
    public void trace(String format, Object arg){
        this.trace(format, new Object[]{arg});
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        this.trace(format, new Object[]{arg1, arg2});
    }

    @Override
    public void trace(String msg, Throwable t){
        this.trace(msg, new Object[]{t});
    }

    @Override
    public void debug(String message, Object... args){

        JsonLogger log = logger.debug()
                .message(message);
        toJson(log, args);

    }

    @Override
    public void debug(String message){

        JsonLogger log = logger.debug()
                .message(message);
        toJson(log);

    }

    @Override
    public void debug(String format, Object arg){
        this.debug(format, new Object[]{arg});
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        this.debug(format, new Object[]{arg1, arg2});
    }

    @Override
    public void debug(String msg, Throwable t){
        this.debug(msg, new Object[]{t});
    }



    @Override
    public void info(String message, Object... args){
        JsonLogger log = logger.info()
                .message(message);
        toJson(log, args);
    }

    @Override
    public void info(String format, Object arg){
        this.info(format, new Object[]{arg});
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        this.info(format, new Object[]{arg1, arg2});
    }

    @Override
    public void info(String msg, Throwable t){
        this.info(msg, new Object[]{t});
    }

    @Override
    public void info(String message){
        JsonLogger log = logger.info()
                .message(message);
        toJson(log);
    }

    @Override
    public void warn(String message, Object... args){
        JsonLogger log = logger.warn()
                .message(message);
        toJson(log, args);
    }

    @Override
    public void warn(String message){
        JsonLogger log = logger.warn()
                .message(message);
        toJson(log);
    }

    @Override
    public void warn(String format, Object arg){
        this.warn(format, new Object[]{arg});
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        this.warn(format, new Object[]{arg1, arg2});
    }

    @Override
    public void warn(String msg, Throwable t){
        this.warn(msg, new Object[]{t});
    }

    @Override
    public void error(String message, Object... args){
        JsonLogger log = logger.error()
                .message(message);
        toJson(log, args);
    }

    @Override
    public void error(String message){
        JsonLogger log = logger.error()
                .message(message);
        toJson(log);
    }

    @Override
    public void error(String format, Object arg){
        this.error(format, new Object[]{arg});
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        this.error(format, new Object[]{arg1, arg2});
    }

    @Override
    public void error(String msg, Throwable t){
        this.error(msg, new Object[]{t});
    }


    @Override
    public <T extends Throwable> T throwing(T throwable){

        JsonLogger log = logger.error()
                .message(throwable.getMessage());
        toJson(log, throwable);
        return super.throwing(throwable);
    }

    @Override
    public void catching(Throwable throwable){

        JsonLogger log = logger.error()
                .message(throwable.getMessage());
        toJson(log, throwable);
        super.catching(throwable);
    }

    private void toJson(JsonLogger log, Object... args){

        for (Object arg: args) {
            if (arg instanceof LogField){
                log.field(((LogField)arg).getKey(), ((LogField)arg).getValue());
            } else if (arg instanceof Map){
                ((Map)arg).forEach((key, value) -> log.field(key.toString(), value));
            }  else if (arg instanceof List){
                log.list("List", (List)arg);
            } else if (arg instanceof Throwable){
                log.exception(((Throwable)arg).getClass().getName(), (Exception) arg);
            }

        }

        log.log();
    }
}
