package com.quantal.javashared.aspects;


import com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders;
import com.quantal.javashared.exceptions.HeaderNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.annotation.AnnotationUtils;
import retrofit2.http.Header;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.joining;

@Aspect
@Configurable
public class RetrofitRequiredHeadersEnforcerAspect {

    private final Set<String> headersToCheckFor;
    private Map<String, Boolean> foundHeaders;
    private  static  String POINTCUT_EXPRESSION = "";

    public RetrofitRequiredHeadersEnforcerAspect(){
        this.headersToCheckFor = new HashSet<>();
        this.foundHeaders = new HashMap<>();
    }
    public RetrofitRequiredHeadersEnforcerAspect(Set<String> headersToCheckFor, String pointcut_expression){

        this.headersToCheckFor = headersToCheckFor;
        POINTCUT_EXPRESSION = pointcut_expression;
        foundHeaders = new HashMap<>();
        if (headersToCheckFor != null){
            headersToCheckFor.forEach( header ->foundHeaders.put(header.toUpperCase(), false));
        }
    }

    @Around("com.quantal.javashared.aspects.RetrofitRequiredHeadersEnforcerAspect.allRetrofitInterfaces()")
    public Object checkHeaders(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Annotation[][] annotations = method.getParameterAnnotations();
        String[] annotationVals = null;

        Set<String> toCheckFor = null;
        boolean bReplaceDefaults = false;

        if(method.getDeclaringClass().isAnnotationPresent(EnforceRequiredHeaders.class)
                || AnnotationUtils.findAnnotation(method,  EnforceRequiredHeaders.class) != null){
            Annotation annot = null;
            if(method.getDeclaringClass().isAnnotationPresent(EnforceRequiredHeaders.class)) {
                AnnotationUtils.findAnnotation(method.getDeclaringClass(), EnforceRequiredHeaders.class);
                annotationVals = (String[]) AnnotationUtils.getValue(annot);
            } else {
                annot = AnnotationUtils.findAnnotation(method, EnforceRequiredHeaders.class);
                annotationVals = (String[]) AnnotationUtils.getValue(annot);
            }
            toCheckFor = new HashSet<>();
            if(annot != null) {
                bReplaceDefaults = ((EnforceRequiredHeaders) annot).replaceDefaults();
            }

            for(String annotationVal: annotationVals) {
                toCheckFor.add(annotationVal.toUpperCase());
            }
            if (bReplaceDefaults) {
                foundHeaders = new HashMap<>();
            }
            toCheckFor.forEach((entry)-> this.foundHeaders.put(entry.toUpperCase(), false));
        }


        for (Annotation[] annotation : annotations) {
            Annotation header = annotation[0];
            if (header != null && header instanceof Header) {
                String heaverAnnotionName = String.valueOf(AnnotationUtils.getValue(header));
                foundHeaders.computeIfPresent(heaverAnnotionName.toUpperCase(), (key, val) -> true);
            }

        }


        boolean bAllHeadersFound = foundHeaders.values()
                .stream()
                .reduce((previous, current) -> previous && current)
                .orElse(false);


        if (!bAllHeadersFound) {
            String msgPattern = "Method %s.%s requires a parameter with the annotation %s that has a value '%s'";
            String errMsg = foundHeaders
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == false)
                    .map(entry -> String.format(msgPattern,method.getDeclaringClass().getName(),method.getName(), Header.class.getName(), entry.getKey()))
                    .collect(joining(".\n"));

            throw new HeaderNotFoundException(errMsg);
        }

        Object result = pjp.proceed();

        return result;

    }

    /*@Pointcut("(execution(* com.quantal..services.api..*(..)) || execution (* com.quantal..services.api..*.*(..)) || execution(* com.quantal.javashared.controller..*(..)))&&" +
            " (" +
            "@target(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders)" +
            "|| @within(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders)" +
            "|| @annotation(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders)" +

            ")")*/
    @Pointcut("target(com.quantal..services.api.*Service+.*) &&" +
            "(@target(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders)" +
            "|| @within(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders)" +
            "|| @annotation(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders))")
    public void allRetrofitInterfaces(){}
}