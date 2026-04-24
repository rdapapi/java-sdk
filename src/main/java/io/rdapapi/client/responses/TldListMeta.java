package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Metadata envelope for the /tlds list endpoint. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TldListMeta {

  private String computedAt;
  private int count;
  private double coverage;
  private TldThresholds thresholds;

  private TldListMeta() {}

  public String getComputedAt() {
    return computedAt;
  }

  public int getCount() {
    return count;
  }

  public double getCoverage() {
    return coverage;
  }

  public TldThresholds getThresholds() {
    return thresholds;
  }
}
