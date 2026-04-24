package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Metadata envelope for the /tlds/{tld} single-TLD endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TldMeta {

  private String computedAt;
  private TldThresholds thresholds;

  private TldMeta() {}

  public String getComputedAt() {
    return computedAt;
  }

  public TldThresholds getThresholds() {
    return thresholds;
  }
}
