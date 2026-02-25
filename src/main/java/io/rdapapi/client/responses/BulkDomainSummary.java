package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class BulkDomainSummary {

  private int total;
  private int successful;
  private int failed;

  private BulkDomainSummary() {}

  public int getTotal() {
    return total;
  }

  public int getSuccessful() {
    return successful;
  }

  public int getFailed() {
    return failed;
  }
}
