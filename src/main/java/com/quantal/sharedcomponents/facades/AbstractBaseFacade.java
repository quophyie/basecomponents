package com.quantal.sharedcomponents.facades;


import com.quantal.sharedcomponents.dto.ResponseDto;
import com.quantal.sharedcomponents.objectmapper.OrikaBeanMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;


public abstract class AbstractBaseFacade {

  //@Autowired
  //@Qualifier("orikaBeanMapper")
  protected OrikaBeanMapper mapper;

  //@Autowired
  //@Qualifier("nullSkippingOrikaBeanMapper")
  protected OrikaBeanMapper nullSkippingMapper;


  private AbstractBaseFacade() {}

  public AbstractBaseFacade(OrikaBeanMapper mapper, OrikaBeanMapper nullSkippingMapper) {
    this.mapper = mapper;
    this.nullSkippingMapper = nullSkippingMapper;
  }
  /**
   * Converts Dto to a ResponseDto and with the supplied Http Status and headears
   * @param reponseDTOData - The type of the Dto
   * @param httpStatus  - The Http status
   * @param httpHeaders - the http headers
   * @param <TResponseDTOData>
   * @return
   */
  public static <TResponseDTOData> ResponseEntity<?> toRESTResponse(TResponseDTOData reponseDTOData, String message, HttpStatus httpStatus, HttpHeaders httpHeaders){

    ResponseEntity<ResponseDto<TResponseDTOData>> response;
    ResponseDto<TResponseDTOData> responseDto = new ResponseDto<>(message,httpStatus.value(),reponseDTOData);
    if (httpHeaders != null){
      response = new ResponseEntity<>(responseDto, httpHeaders, httpStatus);
    } else {
      response = new ResponseEntity<>(responseDto, httpStatus);
    }
    return response;
  }
  /**
   * Converts Dto to a ResponseDto and with the supplied Http Status
   * @param reponseDTOData - The type of the Dto
   * @param httpStatus  - The Http status
   * @param <TResponseDTOData>
   * @return
   */
  public static <TResponseDTOData> ResponseEntity<?> toRESTResponse(TResponseDTOData reponseDTOData, String message, HttpStatus httpStatus){
    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE,  MediaType.APPLICATION_JSON_VALUE);
    return toRESTResponse(reponseDTOData,message, httpStatus,headers);
  }


  /**
   * Converts Dto to a ResponseDto and with a Http Status code of 200
   * @param reponseDTOData - The type of the Dto
   * @param <TResponseDTOData>
   * @return
   */
  public static <TResponseDTOData> ResponseEntity<?> toRESTResponse(TResponseDTOData reponseDTOData, String message){
    return toRESTResponse(reponseDTOData, message, HttpStatus.OK);
  }

  /**
   * Maps a DTO to a model
   * @param source - the source model to map from i.e. The DTO
   * @param clazz  - The closs (type) of the destination model
   * @param <TDTO> - The type of the source object i.e. the DTO
   * @param <TModel> - The type of the model
   * @return
   */
  public  <TDTO, TModel> TModel toModel(TDTO source, Class<TModel> clazz){
    if (source == null || clazz == null)
      return null;
    TModel model = mapper.map(source, clazz);
    model = mapper.map(source, clazz);

    return model;
  }


  /**
   * Maps a DTO to a model given an instance of the model
   * @param source - the source model to map from i.e. The DTO
   * @param model  - The the destination model
   * @param mapNulls - if true, then null values will copied to destination model. If
   *                 false, null values will be skipped
   * @param <TDTO> - The type of the source object i.e. the DTO
   * @param <TModel> - The type of the model
   * @return
   */
  public  <TDTO, TModel> TModel toModel(TDTO source, TModel model, boolean mapNulls){

    if (source == null || model == null)
      return null;


    if (mapNulls){
      mapper.map(source, model);
    } else {
      nullSkippingMapper.map(source, model);
    }
    return model;
  }

  /**
   * Maps a model to a DTO
   * @param source - the source model to map from i.e. The DTO
   * @param clazz  - The closs (type) of the destination model
   * @param <TDTO> - The type of the source object i.e. the DTO
   * @param <TModel> - The type of the model
   * @return
   */

  public  <TModel, TDTO> TDTO toDto(TModel source, Class<TDTO> clazz){
    if (source == null || clazz == null)
      return null;
    TDTO dto = mapper.map(source, clazz);
    return dto;
  }

  /**
   * Maps a model to a DTO given an instance of the com.quantal.sharedcomponents.dto
   * @param source - the source model to map from i.e. The DTO
   * @param <TDTO> - The type of the source object i.e. the DTO
   * @param <TModel> - The type of the model
   * @return
   */

  public  <TModel, TDTO> TDTO toDto(TModel source, TDTO dto){
    if (source == null || dto == null)
      return null;
    mapper.map(source, dto);
    return dto;
  }
}
