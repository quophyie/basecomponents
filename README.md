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
  - **`com.quantal.shared.services.MessageService`** - A service that wraps around **`org.springframework.context.MessageSource`** to 
  provide a simple interface to   **`org.springframework.context.MessageSource`**.
  
  ### Object Mappers
  - **`com.quantal.shared.objectmapper.OrikaBeanMapper`** - An Orika mapper exposed as a Spring Bean .This is a straight copy of
   [dlizarra](https://github.com/dlizarra)'s  [Orika Mapper Spring integration](https://github.com/dlizarra/orika-spring-integration). This class extends
   **`ApplicationContextAware`** and register's itself with the Spring application
   context that its defined in. This mapper will **`not skip nulls`** i.e. When mapping from a **`source`** object
    to a **`destination`** object, it will set **`destination`** object properties
    which  have not been specified in the **`source`** object to null. This could mean
    that unintentional overwritting of **`destination`** object. For example, it will 
    overwrite entity models and setting properties to null when only subset of the
    entity's model is to be updtaed
  - **`com.quantal.shared.objectmapper.NullSkippingOrikaBeanMapper`** - This Orika Mapper extends  **`com.quantal.shared.objectmapper.OrikaBeanMapper`**
    except that this will  **`skip nulls`** and will not unintentionally overwrite properties like  **`com.quantal.shared.objectmapper.OrikaBeanMapper`**
  
  ### Facades
  - **`com.quantal.shared.facades.AbstractBaseFacade`** - This facade is intended for use in **`controllers`**.
  Its delibrately opinionated. Any facade that extends this facade **must call** the constructor
  with an instance of **`com.quantal.shared.objectmapper.OrikaBeanMapper` and an 
  instance of  **`com.quantal.shared.objectmapper.NullSkippingOrikaBeanMapper`** i.e. 
  **`super(orikaBeanMapper, nullSkippingOrikaBeanMapper)`**
  
  ### DTOs (Data Transfer Objects)
  - **`com.quantal.shared.dto.ResponseMessageDto`** - A simple DTO that has a **`message`** and **`code`** properties. This DTO should be used as the 
  body object of the **`ResponseEntity`** returned by **`ResponseEntity`**
  
  - **`com.quantal.shared.dto.ResponseDto`** - ResponseDto that extends **`com.quantal.shared.dto.ResponseMessageDto`**. It has
  message and code as well as a payload (i.e. **`data`** property). This DTO should be used as the
  body object of the **`ResponseEntity`** returned by **`ResponseEntity`**
  
  ### JsonViews
  - **`com.quantal.shared.jsonviews.DefaultViews.ResponseDtoView`** - The **`JsonView`** that is used to filter properties 
  **`com.quantal.shared.dto.ResponseDto`**. Any DTO that extends **`com.quantal.shared.dto.ResponseDto`** must have a **`JsonView`**
  extend this view (i.e.  **`com.quantal.shared.jsonviews.DefaultViews.ResponseDtoView`**) if the properties of the extending DTO are to be serialized
   if a **`JsonView`**  annotation is used to annotate controller methods.
   
  - **`com.quantal.shared.jsonviews.DefaultViews.ResponseMessageDtoView`** - The **`JsonView`** that is used to filter properties 
    **`com.quantal.shared.dto.ResponseMessageDto`**. Any DTO that extends **`com.quantal.shared.dto.ResponseMessageDto`** must have a **`JsonView`**
    extend this view (i.e.  **`com.quantal.shared.jsonviews.DefaultViews.ResponseDtoMessageView`**) if the properties of the extending DTO are to be serialized
     if a **`JsonView`**  annotation is used to annotate controller methods.
     
  ### TestUtils
  - **`com.quantal.shared.util.TestUtils`** - A set of methods used to aid testing. It contains methods for tasks such as extracting Response bodies from 
  **`ResponseEntity`**, converting objects to JSON strings amongst other features
     