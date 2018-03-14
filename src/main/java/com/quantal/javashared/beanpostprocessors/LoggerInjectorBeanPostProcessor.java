package com.quantal.javashared.beanpostprocessors;

import com.quantal.javashared.annotations.logger.InjectLogger;
import com.quantal.javashared.annotations.logger.LoggerType;
import com.quantal.javashared.dto.CommonLogFields;
import com.quantal.javashared.dto.LoggerConfig;
import com.quantal.javashared.dto.LogzioConfig;
import com.quantal.javashared.logger.QuantalLogger;
import com.quantal.javashared.logger.QuantalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public class LoggerInjectorBeanPostProcessor implements BeanPostProcessor{

    private final CommonLogFields commonLogFields;
    private final LogzioConfig logzioConfig;

    public LoggerInjectorBeanPostProcessor(CommonLogFields commonLogFields, LogzioConfig logzioConfig){

        this.commonLogFields = commonLogFields;
        this.logzioConfig = logzioConfig;
    }
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        List<Field> annotations = Arrays.asList(bean.getClass().getDeclaredFields());
        List<Field>  loggerAnnotatedFields = annotations.stream().filter(field -> (
                field.getType() == QuantalLogger.class
                        || field.getType() == Logger.class
                        || field.getType() == XLogger.class)
                && field.getAnnotation(InjectLogger.class) != null)
                .collect(Collectors.toList());

        loggerAnnotatedFields.forEach(field -> {
            Class clazz = field.getDeclaringClass();
            if(!field.isAccessible()) {
                ReflectionUtils.makeAccessible(field);
            }
            Annotation annotation = field.getAnnotation(InjectLogger.class);

            LoggerType loggerType = (LoggerType) AnnotationUtils.getValue(annotation, "value");

            if(field.getType() == QuantalLogger.class) {
                switch (loggerType) {
                    case Logzio: {
                        LoggerConfig loggerConfig = LoggerConfig.builder()
                                .logzioConfig(logzioConfig)
                                .commonLogFields(commonLogFields)
                                .build();
                        ReflectionUtils.setField(field, bean, QuantalLoggerFactory.getLogzioLogger(clazz,loggerConfig));
                        break;
                    }
                    case GoDaddy: {
                        LoggerConfig loggerConfig = LoggerConfig.builder()
                                .commonLogFields(commonLogFields)
                                .build();
                        ReflectionUtils.setField(field, bean, QuantalLoggerFactory.getLogger(clazz, loggerConfig));
                        break;
                    }
                }
            } else {
                if (field.getType() == Logger.class) {
                  ReflectionUtils.setField(field, bean, LoggerFactory.getLogger(clazz));
                } else if ( field.getType() == XLogger.class){
                    ReflectionUtils.setField(field, bean, XLoggerFactory.getXLogger(clazz));
                }
            }

        });
        return bean;
    }
}
