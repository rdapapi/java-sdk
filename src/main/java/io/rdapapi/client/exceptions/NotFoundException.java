package io.rdapapi.client.exceptions;

public class NotFoundException extends RdapApiException {

  private final String reason;
  private final String registrableDomain;

  public NotFoundException(String message, String errorCode) {
    this(message, errorCode, null, null);
  }

  public NotFoundException(
      String message, String errorCode, String reason, String registrableDomain) {
    super(message, 404, errorCode);
    this.reason = reason;
    this.registrableDomain = registrableDomain;
  }

  /** The registry's own wording on a {@code domain_reserved} answer, or null. */
  public String getReason() {
    return reason;
  }

  /** The domain to look up instead on a {@code not_registrable} answer, or null. */
  public String getRegistrableDomain() {
    return registrableDomain;
  }
}
