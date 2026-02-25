package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EntityAutnum {

  private String handle;
  private String name;
  private Integer startAutnum;
  private Integer endAutnum;

  private EntityAutnum() {}

  EntityAutnum(String handle, String name, Integer startAutnum, Integer endAutnum) {
    this.handle = handle;
    this.name = name;
    this.startAutnum = startAutnum;
    this.endAutnum = endAutnum;
  }

  public String getHandle() {
    return handle;
  }

  public String getName() {
    return name;
  }

  public Integer getStartAutnum() {
    return startAutnum;
  }

  public Integer getEndAutnum() {
    return endAutnum;
  }
}
