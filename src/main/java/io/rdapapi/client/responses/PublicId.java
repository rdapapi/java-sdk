package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class PublicId {

  private String type;
  private String identifier;

  private PublicId() {}

  PublicId(String type, String identifier) {
    this.type = type;
    this.identifier = identifier;
  }

  public String getType() {
    return type;
  }

  public String getIdentifier() {
    return identifier;
  }
}
