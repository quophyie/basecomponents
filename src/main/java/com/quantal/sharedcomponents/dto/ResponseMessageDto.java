package com.quantal.sharedcomponents.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.quantal.sharedcomponents.jsonviews.DefaultJsonView;
import lombok.Data;

/**
 * Created by dman on 08/03/2017.
 */
@Data
public class ResponseMessageDto {

  @JsonView(DefaultJsonView.ResponseMessageDtoView.class)
  private String message;
  @JsonView(DefaultJsonView.ResponseMessageDtoView.class)
  private int code;

  public ResponseMessageDto(){}
  public ResponseMessageDto(String message, int code) {
    this.code = code;
    this.message = message;

  }
}

