package io.rdapapi.client;

public final class DomainOptions {

  private boolean follow;
  private boolean whois = true;

  public DomainOptions() {}

  public boolean isFollow() {
    return follow;
  }

  /** Merge in the contacts held by the registrar. Most {@code .com} and {@code .net} want this. */
  public DomainOptions follow(boolean follow) {
    this.follow = follow;
    return this;
  }

  public boolean isWhois() {
    return whois;
  }

  /**
   * Set {@code false} to refuse the WHOIS fallback, so a TLD with no RDAP server throws {@link
   * io.rdapapi.client.exceptions.NotSupportedException} instead of answering over WHOIS. Defaults
   * to {@code true}.
   */
  public DomainOptions whois(boolean whois) {
    this.whois = whois;
    return this;
  }
}
