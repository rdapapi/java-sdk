package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response for the GET /ping liveness probe. */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class PingResponse {

  private String status;

  private PingResponse() {}

  PingResponse(String status) {
    this.status = status;
  }

  /** Always {@code ok} when the API answered. */
  public String getStatus() {
    return status;
  }
}
