package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response envelope for the GET /tlds/{tld} single-TLD endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TldResponse {

  private TldEntry data;
  private TldMeta meta;
  private String etag;

  private TldResponse() {}

  public TldEntry getData() {
    return data;
  }

  public TldMeta getMeta() {
    return meta;
  }

  /**
   * Server-provided ETag. Pass back to {@link io.rdapapi.client.RdapClient#tld(String, TldOptions)}
   * via {@link TldOptions#setIfNoneMatch(String)} to skip unchanged transfers on a later call.
   */
  public String getEtag() {
    return etag;
  }

  /** Internal setter used by {@link io.rdapapi.client.RdapClient} after deserialization. */
  public void setEtag(String etag) {
    this.etag = etag;
  }
}
