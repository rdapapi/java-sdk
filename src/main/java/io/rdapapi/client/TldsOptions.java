package io.rdapapi.client;

/** Options for {@link RdapClient#tlds(TldsOptions)}. */
public final class TldsOptions {

  private String since;
  private String server;
  private String ifNoneMatch;

  public TldsOptions() {}

  public String getSince() {
    return since;
  }

  /** ISO 8601 timestamp. Only TLDs supported after this instant are returned. */
  public TldsOptions since(String since) {
    this.since = since;
    return this;
  }

  public String getServer() {
    return server;
  }

  /** RDAP server hostname filter (case-insensitive). */
  public TldsOptions server(String server) {
    this.server = server;
    return this;
  }

  public String getIfNoneMatch() {
    return ifNoneMatch;
  }

  /**
   * Previous ETag. When it matches, {@link RdapClient#tlds(TldsOptions)} returns {@code null}
   * instead of re-fetching the body.
   */
  public TldsOptions ifNoneMatch(String etag) {
    this.ifNoneMatch = etag;
    return this;
  }

  /** Alias for {@link #ifNoneMatch(String)} used by javadoc references. */
  public void setIfNoneMatch(String etag) {
    this.ifNoneMatch = etag;
  }
}
