package io.rdapapi.client.responses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public final class Registrar {

  private String name;
  private String ianaId;
  private String abuseEmail;
  private String abusePhone;
  private String url;

  private Registrar() {}

  Registrar(String name, String ianaId, String abuseEmail, String abusePhone, String url) {
    this.name = name;
    this.ianaId = ianaId;
    this.abuseEmail = abuseEmail;
    this.abusePhone = abusePhone;
    this.url = url;
  }

  public String getName() {
    return name;
  }

  public String getIanaId() {
    return ianaId;
  }

  public String getAbuseEmail() {
    return abuseEmail;
  }

  public String getAbusePhone() {
    return abusePhone;
  }

  public String getUrl() {
    return url;
  }
}
