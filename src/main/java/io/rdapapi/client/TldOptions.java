package io.rdapapi.client;

/** Options for {@link RdapClient#tld(String, TldOptions)}. */
public final class TldOptions {

  private String ifNoneMatch;

  public TldOptions() {}

  public String getIfNoneMatch() {
    return ifNoneMatch;
  }

  /**
   * Previous ETag. When it matches, {@link RdapClient#tld(String, TldOptions)} returns {@code null}
   * instead of re-fetching the body.
   */
  public TldOptions ifNoneMatch(String etag) {
    this.ifNoneMatch = etag;
    return this;
  }

  /** Alias for {@link #ifNoneMatch(String)} used by javadoc references. */
  public void setIfNoneMatch(String etag) {
    this.ifNoneMatch = etag;
  }
}
