package io.rdapapi.client.exceptions;

/**
 * Thrown when the query targets a namespace not covered by RDAP (HTTP 404).
 *
 * <p>Extends {@link NotFoundException} so existing {@code catch (NotFoundException)} blocks keep
 * working. Catch this class first when you want to distinguish "no RDAP server for this TLD/range"
 * from "namespace covered but no matching record".
 */
public class NotSupportedException extends NotFoundException {

  public NotSupportedException(String message, String errorCode) {
    super(message, errorCode);
  }
}
