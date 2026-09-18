package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class AsnResponse {

  private String handle;
  private String name;
  private String type;
  private Integer startAutnum;
  private Integer endAutnum;
  private String country;
  private List<String> status;
  private Dates dates;
  private Entities entities;
  private List<Remark> remarks;
  private String port43;
  private Redaction redacted;
  private Meta meta;

  private AsnResponse() {}

  public String getHandle() {
    return handle;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public Integer getStartAutnum() {
    return startAutnum;
  }

  public Integer getEndAutnum() {
    return endAutnum;
  }

  /**
   * ISO 3166-1 alpha-2 country code, derived from the contact entities' address: the regional
   * registries carry no top-level country on autnum records. Null when no contact supplies one.
   */
  public String getCountry() {
    return country;
  }

  public List<String> getStatus() {
    return status != null ? Collections.unmodifiableList(status) : Collections.emptyList();
  }

  public Dates getDates() {
    return dates;
  }

  public Entities getEntities() {
    return entities;
  }

  public List<Remark> getRemarks() {
    return remarks != null ? Collections.unmodifiableList(remarks) : Collections.emptyList();
  }

  public String getPort43() {
    return port43;
  }

  /** What the upstream server declared it withheld, or null when it declared nothing. */
  public Redaction getRedacted() {
    return redacted;
  }

  public Meta getMeta() {
    return meta;
  }
}
