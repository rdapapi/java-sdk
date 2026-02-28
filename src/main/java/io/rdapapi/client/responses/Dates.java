package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Dates {

  private String registered;
  private String expires;
  private String updated;

  private Dates() {}

  Dates(String registered, String expires, String updated) {
    this.registered = registered;
    this.expires = expires;
    this.updated = updated;
  }

  public String getRegistered() {
    return registered;
  }

  public String getExpires() {
    return expires;
  }

  public String getUpdated() {
    return updated;
  }

  /** Parse the registered date into an Instant, or null if absent or unparseable. */
  public Instant getRegisteredAt() {
    return parseInstant(registered);
  }

  /** Parse the expiry date into an Instant, or null if absent or unparseable. */
  public Instant getExpiresAt() {
    return parseInstant(expires);
  }

  /** Parse the updated date into an Instant, or null if absent or unparseable. */
  public Instant getUpdatedAt() {
    return parseInstant(updated);
  }

  /**
   * Days until expiration, or null if no expiry date is available.
   *
   * <p>Returns a negative number if the domain has already expired.
   */
  public Long getExpiresInDays() {
    Instant exp = getExpiresAt();
    if (exp == null) {
      return null;
    }
    return ChronoUnit.DAYS.between(Instant.now(), exp);
  }

  private static Instant parseInstant(String value) {
    if (value == null) {
      return null;
    }
    try {
      return Instant.parse(value);
    } catch (Exception e) {
      return null;
    }
  }
}
