package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/** Qualitative bucket describing how often a field is populated in a TLD's RDAP responses. */
public enum AvailabilityLevel {
  ALWAYS("always"),
  USUALLY("usually"),
  SOMETIMES("sometimes"),
  NEVER("never");

  private final String wireName;

  AvailabilityLevel(String wireName) {
    this.wireName = wireName;
  }

  @JsonValue
  public String toWire() {
    return wireName;
  }

  @JsonCreator
  public static AvailabilityLevel fromWire(String value) {
    for (AvailabilityLevel level : values()) {
      if (level.wireName.equals(value)) {
        return level;
      }
    }
    throw new IllegalArgumentException("Unknown availability level: " + value);
  }
}
