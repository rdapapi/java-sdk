package io.rdapapi.client.exceptions;

public class UpstreamException extends RdapApiException {

  public UpstreamException(String message, String errorCode) {
    super(message, 502, errorCode);
  }
}
