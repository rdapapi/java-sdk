package io.rdapapi.client.exceptions;

public class AuthenticationException extends RdapApiException {

  public AuthenticationException(String message, String errorCode) {
    super(message, 401, errorCode);
  }
}
