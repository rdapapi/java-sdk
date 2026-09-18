package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class DomainResponse {

  private String domain;
  private String unicodeName;
  private String handle;
  private List<String> status;
  private Registrar registrar;
  private Dates dates;
  private List<String> nameservers;
  private Boolean dnssec;
  private Entities entities;
  private Redaction redacted;
  private Meta meta;

  private DomainResponse() {}

  DomainResponse(
      String domain,
      String unicodeName,
      String handle,
      List<String> status,
      Registrar registrar,
      Dates dates,
      List<String> nameservers,
      Boolean dnssec,
      Entities entities,
      Redaction redacted,
      Meta meta) {
    this.domain = domain;
    this.unicodeName = unicodeName;
    this.handle = handle;
    this.status = status;
    this.registrar = registrar;
    this.dates = dates;
    this.nameservers = nameservers;
    this.dnssec = dnssec;
    this.entities = entities;
    this.redacted = redacted;
    this.meta = meta;
  }

  public String getDomain() {
    return domain;
  }

  public String getUnicodeName() {
    return unicodeName;
  }

  public String getHandle() {
    return handle;
  }

  public List<String> getStatus() {
    return status != null ? Collections.unmodifiableList(status) : Collections.emptyList();
  }

  public Registrar getRegistrar() {
    return registrar;
  }

  public Dates getDates() {
    return dates;
  }

  public List<String> getNameservers() {
    return nameservers != null
        ? Collections.unmodifiableList(nameservers)
        : Collections.emptyList();
  }

  /**
   * Whether the delegation is signed, or null where the registry publishes no DNSSEC status, as
   * {@code .tr}, {@code .gg} and {@code .nc} do not.
   */
  public Boolean getDnssec() {
    return dnssec;
  }

  public Entities getEntities() {
    return entities;
  }

  /** What the upstream server declared it withheld, or null when it declared nothing. */
  public Redaction getRedacted() {
    return redacted;
  }

  public Meta getMeta() {
    return meta;
  }

  void setMeta(Meta meta) {
    this.meta = meta;
  }
}
