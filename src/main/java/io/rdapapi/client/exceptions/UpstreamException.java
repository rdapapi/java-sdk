package io.rdapapi.client.exceptions;

public class UpstreamException extends RdapApiException {

  private final Integer retryAfter;

  public UpstreamException(String message, String errorCode) {
    this(message, errorCode, null);
  }

  public UpstreamException(String message, String errorCode, Integer retryAfter) {
    super(message, 502, errorCode);
    this.retryAfter = retryAfter;
  }

  /** Seconds until the upstream server may recover, or null when the API did not say. */
  public Integer getRetryAfter() {
    return retryAfter;
  }
}
