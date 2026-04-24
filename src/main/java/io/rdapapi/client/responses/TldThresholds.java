package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Percentage cutoffs used to pick each availability label. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class TldThresholds {

  private double always;
  private double usually;
  private double sometimes;

  private TldThresholds() {}

  public double getAlways() {
    return always;
  }

  public double getUsually() {
    return usually;
  }

  public double getSometimes() {
    return sometimes;
  }
}
