package io.rdapapi.client.exceptions;

public class TemporarilyUnavailableException extends RdapApiException {

  private final Integer retryAfter;

  public TemporarilyUnavailableException(String message, String errorCode, Integer retryAfter) {
    super(message, 503, errorCode);
    this.retryAfter = retryAfter;
  }

  public Integer getRetryAfter() {
    return retryAfter;
  }
}
