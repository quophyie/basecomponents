package com.quantal.basecomponents.util;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quantal.basecomponents.dto.ResponseDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.charset.Charset;

public class TestUtil {

  public static final MediaType APPLICATION_JSON_UTF8 = new MediaType(MediaType.APPLICATION_JSON.getType(), MediaType.APPLICATION_JSON.getSubtype(), Charset.forName("utf8"));

  /**
   * Converts the given object to bytes
   * @param object - The object to convert
   * @return - the bytes of the json
   * @throws IOException
   */
  public static byte[] convertObjectToJsonBytes(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.writeValueAsBytes(object);
  }

  /**
   * Converts the given object to a Json string
   * @param object - The object to convert
   * @return
   * @throws IOException
   */
  public static String convertObjectToJsonString(Object object) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    return mapper.writeValueAsString(object);
  }

  /**
   * Creates a String of  the given length
   * @param length - the length of the string to create
   * @return String - a new String
   */
  public static String createStringWithLength(int length) {
    StringBuilder builder = new StringBuilder();

    for (int index = 0; index < length; index++) {
      builder.append("a");
    }

    return builder.toString();
  }

  /**
   * Returns the data object of the ResponseDto in the response supplied ResponseEntity
   * @param responseEntity
   * @param <ModelT>
   * @return <ModelT> - the data object of the ResponseDto in the response supplied ResponseEntity
   */
  public static <ModelT> ModelT getResponseDtoData(ResponseEntity<?> responseEntity) {
    if (responseEntity!= null && !(responseEntity.getBody() instanceof ResponseDto) ){
      throw new IllegalArgumentException("Argument responseEntity must be instanceof ResponseEntity<ResponseDto>");
    }
    return  ((ResponseDto<ModelT>)responseEntity.getBody()).getData();
  }

  /**
   * Returns the message of the ResponseDto in the response supplied ResponseEntity
   * @param responseEntity
   * @return <ModelT> - the message of the ResponseDto in the response supplied ResponseEntity
   */
  public static  String getResponseDtoMessage(ResponseEntity<?> responseEntity) {
    if (responseEntity!= null && !(responseEntity.getBody() instanceof ResponseDto) ){
      throw new IllegalArgumentException("Argument responseEntity must be instanceof ResponseEntity<ResponseDto>");
    }
    return  ((ResponseDto)responseEntity.getBody()).getMessage();
  }
}