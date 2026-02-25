package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Collections;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class EntityResponse {

  private String handle;
  private String name;
  private String organization;
  private String email;
  private String phone;
  private String address;
  private String contactUrl;
  private String countryCode;
  private List<String> roles;
  private List<String> status;
  private Dates dates;
  private List<Remark> remarks;
  private String port43;
  private List<PublicId> publicIds;
  private Entities entities;
  private List<EntityAutnum> autnums;
  private List<EntityNetwork> networks;
  private Meta meta;

  private EntityResponse() {}

  public String getHandle() {
    return handle;
  }

  public String getName() {
    return name;
  }

  public String getOrganization() {
    return organization;
  }

  public String getEmail() {
    return email;
  }

  public String getPhone() {
    return phone;
  }

  public String getAddress() {
    return address;
  }

  public String getContactUrl() {
    return contactUrl;
  }

  public String getCountryCode() {
    return countryCode;
  }

  public List<String> getRoles() {
    return roles != null ? Collections.unmodifiableList(roles) : Collections.emptyList();
  }

  public List<String> getStatus() {
    return status != null ? Collections.unmodifiableList(status) : Collections.emptyList();
  }

  public Dates getDates() {
    return dates;
  }

  public List<Remark> getRemarks() {
    return remarks != null ? Collections.unmodifiableList(remarks) : Collections.emptyList();
  }

  public String getPort43() {
    return port43;
  }

  public List<PublicId> getPublicIds() {
    return publicIds != null ? Collections.unmodifiableList(publicIds) : Collections.emptyList();
  }

  public Entities getEntities() {
    return entities;
  }

  public List<EntityAutnum> getAutnums() {
    return autnums != null ? Collections.unmodifiableList(autnums) : Collections.emptyList();
  }

  public List<EntityNetwork> getNetworks() {
    return networks != null ? Collections.unmodifiableList(networks) : Collections.emptyList();
  }

  public Meta getMeta() {
    return meta;
  }
}
