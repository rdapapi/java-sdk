package io.rdapapi.client;

public final class DomainOptions {

  private boolean follow;

  public DomainOptions() {}

  public boolean isFollow() {
    return follow;
  }

  public DomainOptions follow(boolean follow) {
    this.follow = follow;
    return this;
  }
}
