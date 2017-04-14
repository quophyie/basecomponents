package com.quantal.sharedcomponents.dto;

import com.fasterxml.jackson.annotation.JsonView;
import com.quantal.sharedcomponents.jsonviews.DefaultJsonView;
import lombok.Data;

/**
 * Created by dman on 08/03/2017.
 */

@Data
public class ResponseDto<TData> extends ResponseMessageDto {

  @JsonView(DefaultJsonView.ResponseDtoView.class)
  private TData data;

  public ResponseDto(String message, int code, TData data){
    super(message, code);
    this.data = data;
  }

  public ResponseDto(TData data) {
    this.data = data;
  }

  public TData getData() {
    return data;
  }

  public void setData(TData data) {
    this.data = data;
  }
}

