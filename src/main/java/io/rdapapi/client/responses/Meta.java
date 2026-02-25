package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Meta {

  private String rdapServer;
  private String rawRdapUrl;
  private boolean cached;
  private String cacheExpires;
  private Boolean followed;
  private String registrarRdapServer;
  private String followError;

  private Meta() {}

  Meta(
      String rdapServer,
      String rawRdapUrl,
      boolean cached,
      String cacheExpires,
      Boolean followed,
      String registrarRdapServer,
      String followError) {
    this.rdapServer = rdapServer;
    this.rawRdapUrl = rawRdapUrl;
    this.cached = cached;
    this.cacheExpires = cacheExpires;
    this.followed = followed;
    this.registrarRdapServer = registrarRdapServer;
    this.followError = followError;
  }

  public String getRdapServer() {
    return rdapServer;
  }

  public String getRawRdapUrl() {
    return rawRdapUrl;
  }

  public boolean isCached() {
    return cached;
  }

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
