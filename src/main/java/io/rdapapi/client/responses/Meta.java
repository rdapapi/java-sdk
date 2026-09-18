package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Meta {

  private String server;
  private String source;
  private String rdapServer;
  private String rawRdapUrl;
  private Boolean cached;
  private String cacheExpires;
  private Boolean followed;
  private String registrarRdapServer;
  private String followError;

  private Meta() {}

  Meta(
      String server,
      String source,
      String rdapServer,
      String rawRdapUrl,
      Boolean cached,
      String cacheExpires,
      Boolean followed,
      String registrarRdapServer,
      String followError) {
    this.server = server;
    this.source = source;
    this.rdapServer = rdapServer;
    this.rawRdapUrl = rawRdapUrl;
    this.cached = cached;
    this.cacheExpires = cacheExpires;
    this.followed = followed;
    this.registrarRdapServer = registrarRdapServer;
    this.followError = followError;
  }

  /**
   * Hostname of the upstream that answered, matching that TLD's {@code /tlds} entry so it can be
   * handed straight to {@link io.rdapapi.client.TldsOptions#server(String)}. Occasionally null on
   * an older cached record.
   */
  public String getServer() {
    return server;
  }

  /**
   * Which protocol answered, {@code rdap} or {@code whois}. Always {@code rdap} except on domain
   * lookups where the TLD has no RDAP server.
   */
  public String getSource() {
    return source;
  }

  /**
   * Base URL of the RDAP server, present only when {@link #getSource()} is {@code rdap}.
   *
   * @deprecated use {@link #getServer()} for the host that answered, or {@link #getRawRdapUrl()}
   *     for the exact endpoint queried.
   */
  @Deprecated
  public String getRdapServer() {
    return rdapServer;
  }

  /**
   * Direct URL to the raw RDAP response. Null when {@link #getSource()} is {@code whois}, which has
   * no URL form, or when a stored snapshot answered.
   */
  public String getRawRdapUrl() {
    return rawRdapUrl;
  }

  /**
   * Whether a cached record answered. Null in the partial meta of a failed bulk entry, which the
   * API omits it from — absence is not "served live".
   */
  public Boolean getCached() {
    return cached;
  }

  /** When the cached record expires. Null for the same reason {@link #getCached()} can be. */
  public String getCacheExpires() {
    return cacheExpires;
  }

  public Boolean getFollowed() {
    return followed;
  }

  public String getRegistrarRdapServer() {
    return registrarRdapServer;
  }

  public String getFollowError() {
    return followError;
  }
}
