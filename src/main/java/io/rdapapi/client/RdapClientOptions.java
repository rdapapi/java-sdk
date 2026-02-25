package io.rdapapi.client;

import java.time.Duration;

public final class RdapClientOptions {

  private String baseUrl = "https://rdapapi.io/api/v1";
  private Duration timeout = Duration.ofSeconds(30);

  public RdapClientOptions() {}

  public String getBaseUrl() {
    return baseUrl;
  }

  public RdapClientOptions baseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
    return this;
  }

  public Duration getTimeout() {
    return timeout;
  }

  public RdapClientOptions timeout(Duration timeout) {
    this.timeout = timeout;
    return this;
  }
}
