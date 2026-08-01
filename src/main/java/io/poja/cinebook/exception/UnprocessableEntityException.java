package io.poja.cinebook.exception;

import org.springframework.http.HttpStatus;

public class UnprocessableEntityException extends ApiException {
  public UnprocessableEntityException(String message) {
    super(message, HttpStatus.UNPROCESSABLE_ENTITY);
  }
}
