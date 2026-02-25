package io.rdapapi.client.exceptions;

public class SubscriptionRequiredException extends RdapApiException {

  public SubscriptionRequiredException(String message, String errorCode) {
    super(message, 403, errorCode);
  }
}
