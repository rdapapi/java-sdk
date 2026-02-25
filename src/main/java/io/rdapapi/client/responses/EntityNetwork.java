package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EntityNetwork {

  private String handle;
  private String name;
  private String startAddress;
  private String endAddress;
  private String ipVersion;
  private List<String> cidr;

  private EntityNetwork() {}

  EntityNetwork(
      String handle,
      String name,
      String startAddress,
      String endAddress,
      String ipVersion,
      List<String> cidr) {
    this.handle = handle;
    this.name = name;
    this.startAddress = startAddress;
    this.endAddress = endAddress;
    this.ipVersion = ipVersion;
    this.cidr = cidr;
  }

  public String getHandle() {
    return handle;
  }

  public String getName() {
    return name;
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

  public List<String> getCidr() {
    return cidr != null ? Collections.unmodifiableList(cidr) : Collections.emptyList();
  }
}
