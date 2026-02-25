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
  private boolean dnssec;
  private Entities entities;
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
      boolean dnssec,
      Entities entities,
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

  public boolean isDnssec() {
    return dnssec;
  }

  public Entities getEntities() {
    return entities;
  }

  public Meta getMeta() {
    return meta;
  }

  void setMeta(Meta meta) {
    this.meta = meta;
  }
}
