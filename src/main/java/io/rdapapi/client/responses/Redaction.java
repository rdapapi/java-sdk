package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.Map;

/**
 * What the upstream server declared it withheld, and by what method.
 *
 * <p>Mirrors the shape of the record it describes, so a claim about {@code
 * entities.registrant.name} sits at {@code getEntities().getOrDefault("registrant",
 * Map.of()).get("name")} — a role the server declared nothing for is absent from the map, and
 * chaining {@code get("registrant").get("name")} on it throws. The whole object is null when the
 * server declared nothing at all, which is not evidence that nothing was withheld.
 *
 * <p>Values are {@link RedactionMethod} strings. A method we do not recognise is passed through
 * unchanged, so never assume the value is one of the four documented ones.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public final class Redaction {

  private String handle;
  private Map<String, String> registrar;
  private Map<String, Map<String, String>> entities;

  private Redaction() {}

  Redaction(
      String handle, Map<String, String> registrar, Map<String, Map<String, String>> entities) {
    this.handle = handle;
    this.registrar = registrar;
    this.entities = entities;
  }

  /** Method used on the record's own handle, or null when the server declared nothing for it. */
  public String getHandle() {
    return handle;
  }

  /** Claims about the top-level registrar object, keyed by field. Domain lookups only. */
  public Map<String, String> getRegistrar() {
    return registrar != null ? Collections.unmodifiableMap(registrar) : Collections.emptyMap();
  }

  /**
   * Claims keyed by contact role, then by field within that contact. Never null, but a role the
   * server declared nothing for has no entry, so reach for the inner map with {@code getOrDefault}.
   */
  public Map<String, Map<String, String>> getEntities() {
    return entities != null ? Collections.unmodifiableMap(entities) : Collections.emptyMap();
  }
}
