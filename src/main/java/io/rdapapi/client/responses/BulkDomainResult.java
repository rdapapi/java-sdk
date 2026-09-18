package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class BulkDomainResult {

  private String domain;
  private String status;
  private DomainResponse data;
  private Meta meta;
  private String error;
  private String message;

  private BulkDomainResult() {}

  public String getDomain() {
    return domain;
  }

  public String getStatus() {
    return status;
  }

  public DomainResponse getData() {
    return data;
  }

  /**
   * Where the answer came from. On a failed entry this is the partial {@code server} and {@code
   * source} naming the upstream that was tried, and it is null when the entry failed before an
   * upstream was chosen, as {@code invalid_domain} does. On a successful entry it is also on {@link
   * #getData()}.
   */
  public Meta getMeta() {
    return meta;
  }

  public String getError() {
    return error;
  }

  public String getMessage() {
    return message;
  }
}
