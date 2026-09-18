package io.rdapapi.client.exceptions;

/**
 * Thrown when the request is refused for the account (HTTP 403).
 *
 * <p>Covers {@code subscription_required}, {@code plan_upgrade_required} and {@code forbidden} (a
 * blocked IP, which subscribing does not lift). Read {@link #getErrorCode()} to tell them apart
 * before telling anyone to subscribe.
 */
public class SubscriptionRequiredException extends RdapApiException {

  public SubscriptionRequiredException(String message, String errorCode) {
    super(message, 403, errorCode);
  }
}
