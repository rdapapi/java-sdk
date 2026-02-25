package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class IpResponse {

  private String handle;
  private String name;
  private String type;
  private String startAddress;
  private String endAddress;
  private String ipVersion;
  private String parentHandle;
  private String country;
  private List<String> status;
  private Dates dates;
  private Entities entities;
  private List<String> cidr;
  private List<Remark> remarks;
  private String port43;
  private Meta meta;

  private IpResponse() {}

  public String getHandle() {
    return handle;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  public String getStartAddress() {
    return startAddress;
  }

  public String getEndAddress() {
    return endAddress;
  }

  public String getIpVersion() {
    return ipVersion;
  }

  public String getParentHandle() {
    return parentHandle;
  }

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

  public List<String> getCidr() {
    return cidr != null ? Collections.unmodifiableList(cidr) : Collections.emptyList();
  }

  public List<Remark> getRemarks() {
    return remarks != null ? Collections.unmodifiableList(remarks) : Collections.emptyList();
  }

  public String getPort43() {
    return port43;
  }

  public Meta getMeta() {
    return meta;
  }
}
