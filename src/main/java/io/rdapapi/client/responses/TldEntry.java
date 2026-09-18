package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** A single TLD entry from the /tlds catalog. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TldEntry {

  private String tld;
  private String protocol;
  private String supportedSince;
  private String server;
  private String rdapServerHost;
  private String rdapServerUrl;
  private FieldAvailability fieldAvailability;

  private TldEntry() {}

  public String getTld() {
    return tld;
  }

  /**
   * Which protocol answers for this TLD, {@code rdap} or {@code whois}. {@code whois} marks the
   * ccTLDs IANA lists no RDAP server for.
   */
  public String getProtocol() {
    return protocol;
  }

  public String getSupportedSince() {
    return supportedSince;
  }

  /**
   * Hostname of the upstream that answers for this TLD. This is what {@link
   * io.rdapapi.client.TldsOptions#server(String)} filters on, and what a lookup's {@link
   * Meta#getServer()} returns.
   */
  public String getServer() {
    return server;
  }

  /**
   * Null when {@link #getProtocol()} is {@code whois}.
   *
   * @deprecated superseded by {@link #getServer()}.
   */
  @Deprecated
  public String getRdapServerHost() {
    return rdapServerHost;
  }

  /** Null when {@link #getProtocol()} is {@code whois}, which has no URL form. */
  public String getRdapServerUrl() {
    return rdapServerUrl;
  }

  /** {@code null} when the API does not yet have enough observations for this TLD. */
  public FieldAvailability getFieldAvailability() {
    return fieldAvailability;
  }
}
