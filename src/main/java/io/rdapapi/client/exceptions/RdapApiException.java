package io.rdapapi.client.exceptions;

public class RdapApiException extends RuntimeException {

  private final int statusCode;
  private final String errorCode;

  public RdapApiException(String message, int statusCode, String errorCode) {
    super(message);
    this.statusCode = statusCode;
    this.errorCode = errorCode;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getErrorCode() {
    return errorCode;
  }
}
