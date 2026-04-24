package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** How often each common domain field is populated in a TLD's RDAP responses. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class FieldAvailability {

  private AvailabilityLevel registrar;
  private AvailabilityLevel registeredAt;
  private AvailabilityLevel expiresAt;
  private AvailabilityLevel nameservers;
  private AvailabilityLevel status;

  private FieldAvailability() {}

  public AvailabilityLevel getRegistrar() {
    return registrar;
  }

  public AvailabilityLevel getRegisteredAt() {
    return registeredAt;
  }

  public AvailabilityLevel getExpiresAt() {
    return expiresAt;
  }

  public AvailabilityLevel getNameservers() {
    return nameservers;
  }

  public AvailabilityLevel getStatus() {
    return status;
  }
}
