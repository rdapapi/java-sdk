package io.rdapapi.client.exceptions;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Thrown when the request body fails validation (HTTP 422, {@code request_failed}).
 *
 * <p>{@link #getErrors()} names the offending fields — {@code domains} is the usual key on a bulk
 * lookup, and {@code follow}, {@code whois} or {@code domains.N} can also appear.
 */
public class RequestFailedException extends RdapApiException {

  private final transient Map<String, List<String>> errors;

  public RequestFailedException(
      String message, String errorCode, Map<String, List<String>> errors) {
    super(message, 422, errorCode);
    this.errors = errors;
  }

  /** Per-field validation messages keyed by field path. Empty when the server named no field. */
  public Map<String, List<String>> getErrors() {
    return errors != null ? Collections.unmodifiableMap(errors) : Collections.emptyMap();
  }
}
