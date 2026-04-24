package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

/** Response envelope for the GET /tlds list endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TldListResponse {

  private List<TldEntry> data;
  private TldListMeta meta;
  private String etag;

  private TldListResponse() {}

  public List<TldEntry> getData() {
    return data != null ? data : Collections.emptyList();
  }

  public TldListMeta getMeta() {
    return meta;
  }

  /**
   * Server-provided ETag. Pass back to {@link io.rdapapi.client.RdapClient#tlds(TldsOptions)} via
   * {@link TldsOptions#setIfNoneMatch(String)} to skip unchanged transfers on a later call.
   */
  public String getEtag() {
    return etag;
  }

  /** Internal setter used by {@link io.rdapapi.client.RdapClient} after deserialization. */
  public void setEtag(String etag) {
    this.etag = etag;
  }
}
