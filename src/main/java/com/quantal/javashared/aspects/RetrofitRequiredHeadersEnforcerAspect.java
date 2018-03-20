package com.quantal.javashared.aspects;


import com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders;
import com.quantal.javashared.exceptions.HeaderNotFoundException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
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
public class RetrofitRequiredHeadersEnforcerAspect {

    private final Set<String> headersToCheckFor;
    private final Map<String, Boolean> foundHeaders;

    public RetrofitRequiredHeadersEnforcerAspect(){
        this.headersToCheckFor = new HashSet<>();
        foundHeaders = new HashMap<>();
    }
    public RetrofitRequiredHeadersEnforcerAspect(Set<String> headersToCheckFor){

        this.headersToCheckFor = headersToCheckFor;
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

        if(method.getDeclaringClass().isAnnotationPresent(EnforceRequiredHeaders.class)){

        }


         Set<String> toCheckFor = null;
        for (Annotation[] annotation : annotations) {
            Annotation annotationToCheckFoor = annotation[0];
            if (annotationToCheckFoor != null && annotationToCheckFoor instanceof EnforceRequiredHeaders) {
                String[] annotationVals = (String[]) AnnotationUtils.getValue(annotationToCheckFoor);
                for(String annotationVal: annotationVals) {
                    toCheckFor = new HashSet<String>() {{
                        add(annotationVal.toUpperCase());
                    }};
                }

            }

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
            String msgPattern = "Method %s requires paramter(s) with the annotation %s";
            String errMsg = foundHeaders
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == false)
                    .map(entry -> String.format(msgPattern, method.getName(), entry.getKey()))
                    .collect(joining(".\n"));

            throw new HeaderNotFoundException(errMsg);
        }

        Object result = pjp.proceed();

        return result;

    }

    @Pointcut("@within(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders)" +
            "|| @target(com.quantal.javashared.annotations.requestheaders.EnforceRequiredHeaders)")
    public void allRetrofitInterfaces(){}
}