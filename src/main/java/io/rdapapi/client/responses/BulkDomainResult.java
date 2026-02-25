package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class BulkDomainResult {

  private String domain;
  private String status;
  private DomainResponse data;
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

  public String getError() {
    return error;
  }

  public String getMessage() {
    return message;
  }
}
