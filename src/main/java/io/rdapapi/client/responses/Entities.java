package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Entities {

  private Contact registrant;
  private Contact administrative;
  private Contact technical;
  private Contact billing;
  private Contact abuse;

  private Entities() {}

  Entities(
      Contact registrant,
      Contact administrative,
      Contact technical,
      Contact billing,
      Contact abuse) {
    this.registrant = registrant;
    this.administrative = administrative;
    this.technical = technical;
    this.billing = billing;
    this.abuse = abuse;
  }

  public Contact getRegistrant() {
    return registrant;
  }

  public Contact getAdministrative() {
    return administrative;
  }

  public Contact getTechnical() {
    return technical;
  }

  public Contact getBilling() {
    return billing;
  }

  public Contact getAbuse() {
    return abuse;
  }
}
