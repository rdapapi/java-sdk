package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class BulkDomainResponse {

  private List<BulkDomainResult> results;
  private BulkDomainSummary summary;

  private BulkDomainResponse() {}

  public List<BulkDomainResult> getResults() {
    return results != null ? Collections.unmodifiableList(results) : Collections.emptyList();
  }

  public BulkDomainSummary getSummary() {
    return summary;
  }
}
