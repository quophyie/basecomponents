package com.quantal.basecomponents.dto;

import lombok.Data;

/**
 * Created by dman on 08/03/2017.
 */
@Data
public class ResponseMessageDto {
  private String message;
  private int code;

  public ResponseMessageDto(){}
  public ResponseMessageDto(String message, int code) {
    this.code = code;
    this.message = message;

  }
}

