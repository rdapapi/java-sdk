package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Contact {

  private String handle;
  private String name;
  private String organization;
  private String email;
  private String phone;
  private String address;
  private String contactUrl;
  private String countryCode;

  private Contact() {}

  Contact(
      String handle,
      String name,
      String organization,
      String email,
      String phone,
      String address,
      String contactUrl,
      String countryCode) {
    this.handle = handle;
    this.name = name;
    this.organization = organization;
    this.email = email;
    this.phone = phone;
    this.address = address;
    this.contactUrl = contactUrl;
    this.countryCode = countryCode;
  }

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
}
