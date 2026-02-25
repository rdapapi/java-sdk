package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class NameserverResponse {

  private String ldhName;
  private String unicodeName;
  private String handle;
  private IpAddresses ipAddresses;
  private List<String> status;
  private Dates dates;
  private Entities entities;
  private Meta meta;

  private NameserverResponse() {}

  public String getLdhName() {
    return ldhName;
  }

  public String getUnicodeName() {
    return unicodeName;
  }

  public String getHandle() {
    return handle;
  }

  public IpAddresses getIpAddresses() {
    return ipAddresses;
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

  public Meta getMeta() {
    return meta;
  }
}
