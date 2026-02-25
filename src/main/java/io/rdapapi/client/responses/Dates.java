package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
}
