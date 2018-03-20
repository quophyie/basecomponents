# Shared Components
A set of common and shared components and utilities. The components in this library 
are delibrately minimal to prevent microservices and components tightly 
coupled to this library.

## NOTE: 
Before adding any new features to this shared components, 
please **`THINK DEEPLY`** about whether the new feature **`TRULY BELONGS`**
in these shared components and whether it is truly generic enough to be in this library


## Library Content

  ### Services
  - **`com.quantal.javashared.services.MessageService`** - A service that wraps around **`org.springframework.context.MessageSource`** to 
  provide a simple interface to   **`org.springframework.context.MessageSource`**.
  
  ### Asynchronous Controllers Returning CompletableFutures
  
  Async controllers that return **`CompletableFutures`** should extend **`BaseControllerAsync`**. This **`BaseControllerAsync`**
  makes a available the methods **`applyJsonView`** and **`applyJsonViewAsync`**. These allow us to apply **`@JsonView`** annotations to controller methods 
  that return   **`CompletableFutures`** . For example
  
  ```java
  
    @GetMapping(value="/{userId}")
    public CompletableFuture<ResponseEntity> findUserbyId(@PathVariable Long userId){
      return userManagementFacade
              .findUserById(userId)
              .thenApply(responseEntity -> applyJsonView(responseEntity, UserViews.CreatedAndUpdatedUserView.class, objectMapper));
    }


   ```
  

  
  ### Object Mappers
  - **`com.quantal.javashared.objectmapper.OrikaBeanMapper`** - An Orika mapper exposed as a Spring Bean .This is a straight copy of
   [dlizarra](https://github.com/dlizarra)'s  [Orika Mapper Spring integration](https://github.com/dlizarra/orika-spring-integration). This class extends
   **`ApplicationContextAware`** and register's itself with the Spring application
   context that its defined in. This mapper will **`not skip nulls`** i.e. When mapping from a **`source`** object
    to a **`destination`** object, it will set **`destination`** object properties
    which  have not been specified in the **`source`** object to null. This could mean
    that unintentional overwritting of **`destination`** object. For example, it will 
    overwrite entity models and setting properties to null when only subset of the
    entity's model is to be updtaed
  - **`com.quantal.javashared.objectmapper.NullSkippingOrikaBeanMapper`** - This Orika Mapper extends  **`com.quantal.javashared.objectmapper.OrikaBeanMapper`**
    except that this will  **`skip nulls`** and will not unintentionally overwrite properties like  **`com.quantal.javashared.objectmapper.OrikaBeanMapper`**
  
  ### Facades
  - **`com.quantal.javashared.facades.AbstractBaseFacade`** - This facade is intended for use in **`controllers`**.
  Its delibrately opinionated. Any facade that extends this facade **must call** the constructor
  with an instance of **`com.quantal.javashared.objectmapper.OrikaBeanMapper` and an 
  instance of  **`com.quantal.javashared.objectmapper.NullSkippingOrikaBeanMapper`** i.e. 
  **`super(orikaBeanMapper, nullSkippingOrikaBeanMapper)`**
  
  ### DTOs (Data Transfer Objects)
  - **`com.quantal.javashared.dto.ResponseMessageDto`** - A simple DTO that has a **`message`** and **`code`** properties. This DTO should be used as the 
  body object of the **`ResponseEntity`** returned by **`ResponseEntity`**
  
  - **`com.quantal.javashared.dto.ResponseDto`** - ResponseDto that extends **`com.quantal.javashared.dto.ResponseMessageDto`**. It has
  message and code as well as a payload (i.e. **`data`** property). This DTO should be used as the
  body object of the **`ResponseEntity`** returned by **`ResponseEntity`**
  
  ### JsonViews
  - **`com.quantal.javashared.jsonviews.DefaultViews.ResponseDtoView`** - The **`JsonView`** that is used to filter properties 
  **`com.quantal.javashared.dto.ResponseDto`**. Any DTO that extends **`com.quantal.javashared.dto.ResponseDto`** must have a **`JsonView`**
  extend this view (i.e.  **`com.quantal.javashared.jsonviews.DefaultViews.ResponseDtoView`**) if the properties of the extending DTO are to be serialized
   if a **`JsonView`**  annotation is used to annotate controller methods.
   
  - **`com.quantal.javashared.jsonviews.DefaultViews.ResponseMessageDtoView`** - The **`JsonView`** that is used to filter properties 
    **`com.quantal.javashared.dto.ResponseMessageDto`**. Any DTO that extends **`com.quantal.javashared.dto.ResponseMessageDto`** must have a **`JsonView`**
    extend this view (i.e.  **`com.quantal.javashared.jsonviews.DefaultViews.ResponseDtoMessageView`**) if the properties of the extending DTO are to be serialized
     if a **`JsonView`**  annotation is used to annotate controller methods.
   
  ## Configuring the Quantal Logger
   The **`Quantal Logger`** wraps around the  [GoDaddy Logger](https://github.com/godaddy/godaddy-logger) and the [Logzio Java Sender](https://github.com/logzio/logzio-java-sender)
   and makes simple it for us to create structured logs that are sent to [Logz.io](http://logz.io). Among other things, the **`Quantal Logger`**  allows you to 
   define common log fields that should be included in all logs and also define fields that are required in all logs before the log is considered
   a valid log line (**`traceId`** and **`event`** by default).The **`LoggerConfig`** object is used to set configurations for the logger. For example 
   if you do not want the **`traceId`** and **`event`** as required log fields in every log, you can set  **`eventIsRequired`** and **`traceIdIsRequired`** to `false`. As another example if you wanted tp configure
   other custom required fields, you can add these new custom required fields to the **`requiredLogFields`** map on the **`LoggerConfig`** object.  The [GoDaddy Logger](https://github.com/godaddy/godaddy-logger) is itself an 
   implementation of **`org.slf4j.Logger`**. You can configure the **`Quantal Logger`** manually using the **`com.quantal.javashared.logger.QuantalLoggerFactory`** for  each class.
   However, you create the configuration that is used globally by the  **`Quantal Logger`** and then
   use  **`@InjectLogger`** annotation inject a class specific logger. The **`@InjectLogger`** annotation is is implemented as a bean post processor which
   needs to be configured. The bean post processor that is used to by the **`@InjectLogger`** annotation is 
   implemented in **`com.quantal.javashared.beanpostprocessors.LoggerInjectorBeanPostProcessor`.**. To configure the **`com.quantal.javashared.beanpostprocessors.LoggerInjectorBeanPostProcessor`** and make the 
   **`@InjectLogger`** annotation functional , copy and paste the following into **`@Configuration`** annotated classes
   
   ```java
  
      @Bean
      public CommonLogFields commonLogFields() throws UnknownHostException {
          String hostname = InetAddress.getLocalHost().getHostName();
          return new CommonLogFields(
                  "java",
                  "spring-boot",
                  springVersion,
                  moduleName,
                  hostname,
                  moduleVersion,
                  Locale.UK.toString(),
                  Instant.now().toString()
                  );
      }
  
      @Bean
      public LogzioConfig logzioConfig(@Value("${logzio.token}") String logzioToken) {
          return QuantalLoggerFactory.createDefaultLogzioConfig(logzioToken, Optional.empty(), Optional.empty());
      }
  
      @Bean
      public LoggerConfig loggerConfig(LogzioConfig logzioConfig, CommonLogFields commonLogFields){
          LoggerConfig loggerConfig = LoggerConfig.builder().build();
          loggerConfig.setLogzioConfig(logzioConfig);
          loggerConfig.setCommonLogFields(commonLogFields);
          return loggerConfig;
      }
      @Bean
      public LoggerInjectorBeanPostProcessor loggerInjectorBeanPostProcessor(CommonLogFields commonLogFields, LogzioConfig logzioConfig){
          return new LoggerInjectorBeanPostProcessor(commonLogFields, logzioConfig);
      }
  ```
  
  To use **`@InjectLogger`** annotation, you will do something akin to the following 
  
  ```java
  
  @org.springframework.stereotype.Component
  public class MyClass{
       @com.quantal.javashared.annotations.logger.InjectLogger
        private QuantalLogger logger;
       
       public void myMethod(){
           logger.with("traceId", "SomeTraceId")
                  .with("event", "SOME_EVENT")
                  .debug("doing some task");
       }
  }
  
  ```
  
  **Manual Configuration**
  
  ```java

CommonLogFields commonLogFields =  new CommonLogFields();
		commonLogFields.setFrameworkVersion(new String("1.0.1"));
		commonLogFields.setFramework(1.00);
		LoggerConfig loggerConfig = LoggerConfig.builder()
                                                .commonLogFields(commonLogFields)
				                                .logzioConfig(QuantalLoggerFactory.createDefaultLogzioConfig("MY_LOGZIO_TOKEN", Optional.of(true), Optional.empty()))
				                                .build();
		QuantalLogger logger = QuantalLoggerFactory.getLogzioLogger(MyClass.class, loggerConfig);
		Exception nullPointerEx = new NullPointerException("Some NullPointerException");
		logger.throwing(nullPointerEx, new LogEvent("EXCEPTION_EVENT"), new LogField(SUB_EVENT_KEY, String.format("SOME_EX_SUBEVENT %s", nullPointerEx.getMessage())), new LogTraceId("TEST_EX_TRACE_ID"));
		logger.error(new ObjectAppendingMarker("TestMarker", "testMarkerFieldName"), "test markerMsg", new NullPointerException(), new LogTraceId("TEST_TRACE_ID"), new LogEvent("TEST_EVENT"));
		logger.with(new LogTraceId("TEST_TRACE_ID")).with(new LogEvent("TEST_EVENT")).with("StringTest1").with("StringTest2").info("Some string message");
```
  
     
  ### TestUtils
  - **`com.quantal.javashared.util.TestUtils`** - A set of methods used to aid testing. It contains methods for tasks such as extracting Response bodies from 
  **`ResponseEntity`**, converting objects to JSON strings amongst other features
     