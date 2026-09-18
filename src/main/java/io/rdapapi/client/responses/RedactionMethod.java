package io.rdapapi.client.responses;

/**
 * The redaction methods RFC 9537 defines, as they appear in {@link Redaction}.
 *
 * <p>These are constants rather than an enum because an unrecognised method from a server is passed
 * through unchanged.
 */
public final class RedactionMethod {

  /** The value was deleted. */
  public static final String REMOVAL = "removal";

  /** The value was blanked. */
  public static final String EMPTY_VALUE = "emptyValue";

  /** The value was truncated. */
  public static final String PARTIAL_VALUE = "partialValue";

  /** A substitute was published in place of the real value. */
  public static final String REPLACEMENT_VALUE = "replacementValue";

  private RedactionMethod() {}
}
