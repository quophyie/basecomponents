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
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.joining;

/**
 * This is an ASPECTJ ASPECT AND IS INTENDED TO BE USED AS AN ASPECTJ ASPECT
 * This aspect ensures that headers defined as the value parameter of the {@code EnforceRequiredHeaders} or the values defined
 * in the set {@code defaultHeadersToCheckFor} of the {@code RetrofitRequiredHeadersEnforcerAspectJAspect} constructor are provided as headers
 * in methods defined as {@code Retrofit} service headers
 */
@Aspect
@Configurable
public class RetrofitRequiredHeadersEnforcerAspectJAspect {

    private final Set<String> defaultHeadersToCheckFor;
    private final Set<String> apiServiesPackagesRegexPatterns;
    private Map<String, Boolean> foundHeaders;

    public RetrofitRequiredHeadersEnforcerAspectJAspect(){
        this.apiServiesPackagesRegexPatterns = new HashSet<>();
        apiServiesPackagesRegexPatterns.add("com.quantal.*");
        this.defaultHeadersToCheckFor = new HashSet<>();
        this.foundHeaders = new HashMap<>();
    }

    /**
     *
     * @param defaultHeadersToCheckFor Set - defines the names of headers that are required on a {@code Retrofit} service / api method parameters
     *                          when {@code EnforceRequiredHeaders} annotation is declared on the  {@code Retrofit} service / api method
     *                          or the {@code EnforceRequiredHeaders} annotation is declared on the  {@code Retrofit} service / api class
     * @param apiServiesPackagesRegexPatterns Set - a set of regex patterns that describes the packages that contain the
     *                                         {@code Retrofit} service / api methods
     */
    public RetrofitRequiredHeadersEnforcerAspectJAspect(Set<String> defaultHeadersToCheckFor, Set<String> apiServiesPackagesRegexPatterns){

        this.defaultHeadersToCheckFor = defaultHeadersToCheckFor;
        this.apiServiesPackagesRegexPatterns = apiServiesPackagesRegexPatterns;
        foundHeaders = new HashMap<>();
        if (defaultHeadersToCheckFor != null){
            defaultHeadersToCheckFor.forEach( header ->foundHeaders.put(header.toUpperCase(), false));
        }
    }

    @Around("javaProxyInvokeCallsInRetrofit()")
    public Object checkHeaders(ProceedingJoinPoint pjp) throws Throwable {

        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Method apiMethod = null;
        Object uncastMethod = Arrays.stream(pjp.getArgs())
                .filter(arg -> {
                    if (arg instanceof Method){
                        // Check whether the retrofit invoked method is one
                        // an api method call defined in our apiServiesPackagesRegexPatterns set
                    return apiServiesPackagesRegexPatterns.stream()
                            .filter(apiPackageNamePattern ->
                                        Pattern.compile(apiPackageNamePattern)
                                        .matcher(((Method)arg).getDeclaringClass().getName())
                                        .find()
                                    ).count() > 0;

                    }
                    return  false;
                })
                .findFirst()
                .orElse(null);

        // If we cannot find the retrofit api / service method, we dont try and apply our logic so let the aspect continue
        if (uncastMethod == null){
            return pjp.proceed();
        } else {
            apiMethod = (Method) uncastMethod;
        }

        Annotation[][] apiMethodAnnotations = apiMethod.getParameterAnnotations();
        String[] enforceRequiredHeadersAnnotationValues = null;

        final Set<String> requiredheaders = new HashSet<>();
        boolean bReplaceDefaults = false;

        if(((Method)apiMethod).getDeclaringClass().isAnnotationPresent(EnforceRequiredHeaders.class)
                || AnnotationUtils.findAnnotation(apiMethod,  EnforceRequiredHeaders.class) != null){
            Annotation enforceRequiredHeadersAnnotation = null;

            //Check whether EnforceRequiredHeaders annotation is declared at class level
            if(((Method)apiMethod).getDeclaringClass().isAnnotationPresent(EnforceRequiredHeaders.class)) {
                enforceRequiredHeadersAnnotation = AnnotationUtils.findAnnotation(((Method)apiMethod).getDeclaringClass(), EnforceRequiredHeaders.class);
                enforceRequiredHeadersAnnotationValues = (String[]) AnnotationUtils.getValue(enforceRequiredHeadersAnnotation);
            } else {
                // the EnforceRequiredHeaders annotation is declared at method level
                enforceRequiredHeadersAnnotation = AnnotationUtils.findAnnotation(apiMethod, EnforceRequiredHeaders.class);
                enforceRequiredHeadersAnnotationValues = (String[]) AnnotationUtils.getValue(enforceRequiredHeadersAnnotation);
            }


            if(enforceRequiredHeadersAnnotation != null) {
                bReplaceDefaults = ((EnforceRequiredHeaders) enforceRequiredHeadersAnnotation).replaceDefaults();
            }

            for(String annotationVal: enforceRequiredHeadersAnnotationValues) {
                requiredheaders.add(annotationVal.toUpperCase());
            }

            // bReplaceDefaults == true means that we dont include headers found defaultHeadersToCheckFor set when
            // when checking for all the required headers
            // IfbReplaceDefaults == false, then we  include headers found defaultHeadersToCheckFor set when
            // when checking for all the required headers
            if (!bReplaceDefaults) {
                requiredheaders.addAll(defaultHeadersToCheckFor);
            }

            foundHeaders = new HashMap<>();
            requiredheaders.forEach((entry)-> this.foundHeaders.put(entry.toUpperCase(), false));
        }


        for (Annotation[] annotation : apiMethodAnnotations) {
            Annotation headerAnnotation = annotation[0];
            if (headerAnnotation != null && ((headerAnnotation instanceof Proxy) ||(headerAnnotation instanceof Header)) ) {
                String  headerAnnotionName = String.valueOf(AnnotationUtils.getValue(headerAnnotation));
                foundHeaders.computeIfPresent(headerAnnotionName.toUpperCase(), (key, val) -> true);
            }
        }

        boolean bAllRequiredHeadersFound = foundHeaders.values()
                .stream()
                .reduce((previous, current) -> previous && current)
                .orElse(false);


        if (!bAllRequiredHeadersFound) {
            String msgPattern = "Method %s.%s requires a parameter with the annotation %s (i.e a Http Header) and the header name must be '%s'";
            Method finalApiMethod = apiMethod;
            String errMsg = foundHeaders
                    .entrySet()
                    .stream()
                    .filter(entry -> entry.getValue() == false)
                    .map(entry -> String.format(msgPattern,finalApiMethod.getDeclaringClass().getName(), finalApiMethod.getName(), Header.class.getName(), entry.getKey()))
                    .collect(joining(".\n"));

            throw new HeaderNotFoundException(errMsg);
        }

        Object result = pjp.proceed();

        return result;

    }

    @Pointcut("execution(* retrofit2.Retrofit..*.*(..))")
    public void retrofitApiMethodCalls(){}
    @Pointcut("execution(* java.lang.reflect.InvocationHandler.invoke(..)) && cflow(retrofitApiMethodCalls()) ")
    public void javaProxyInvokeCallsInRetrofit(){
    }
}