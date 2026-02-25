package io.rdapapi.client.exceptions;

public class RateLimitException extends RdapApiException {

  private final Integer retryAfter;

  public RateLimitException(String message, String errorCode, Integer retryAfter) {
    super(message, 429, errorCode);
    this.retryAfter = retryAfter;
  }

  public Integer getRetryAfter() {
    return retryAfter;
  }
}
