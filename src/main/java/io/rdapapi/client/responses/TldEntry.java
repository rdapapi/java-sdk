package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A single TLD entry from the /tlds catalog. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TldEntry {

  private String tld;
  private String supportedSince;
  private String rdapServerHost;
  private String rdapServerUrl;
  private FieldAvailability fieldAvailability;

  private TldEntry() {}

  public String getTld() {
    return tld;
  }

  public String getSupportedSince() {
    return supportedSince;
  }

  public String getRdapServerHost() {
    return rdapServerHost;
  }

  public String getRdapServerUrl() {
    return rdapServerUrl;
  }

  /** {@code null} when the API does not yet have enough observations for this TLD. */
  public FieldAvailability getFieldAvailability() {
    return fieldAvailability;
  }
}
