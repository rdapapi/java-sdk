package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class IpAddresses {

  private List<String> v4;
  private List<String> v6;

  private IpAddresses() {}

  IpAddresses(List<String> v4, List<String> v6) {
    this.v4 = v4;
    this.v6 = v6;
  }

  public List<String> getV4() {
    return v4 != null ? Collections.unmodifiableList(v4) : Collections.emptyList();
  }

  public List<String> getV6() {
    return v6 != null ? Collections.unmodifiableList(v6) : Collections.emptyList();
  }
}
