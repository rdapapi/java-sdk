package io.rdapapi.client.exceptions;

public class NotFoundException extends RdapApiException {

  public NotFoundException(String message, String errorCode) {
    super(message, 404, errorCode);
  }
}
