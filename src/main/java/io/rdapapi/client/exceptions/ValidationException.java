package io.rdapapi.client.exceptions;

public class ValidationException extends RdapApiException {

  public ValidationException(String message, String errorCode) {
    super(message, 400, errorCode);
  }
}
