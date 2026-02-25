package io.rdapapi.client;

import static org.assertj.core.api.Assertions.*;

import io.rdapapi.client.responses.*;
import org.junit.jupiter.api.Test;

class ResponsesTest {

  @Test
  void parseDomainResponse() throws Exception {
    DomainResponse r = RdapClient.MAPPER.readValue(Fixtures.domainResponse(), DomainResponse.class);
    assertThat(r.getDomain()).isEqualTo("google.com");
    assertThat(r.getHandle()).isEqualTo("2138514_DOMAIN_COM-VRSN");
    assertThat(r.getStatus()).containsExactly("client delete prohibited");
    assertThat(r.getRegistrar().getName()).isEqualTo("MarkMonitor Inc.");
    assertThat(r.getRegistrar().getIanaId()).isEqualTo("292");
    assertThat(r.getDates().getRegistered()).isEqualTo("1997-09-15T04:00:00Z");
    assertThat(r.getDates().getExpires()).isEqualTo("2028-09-14T04:00:00Z");
    assertThat(r.getDates().getUpdated()).isNull();
    assertThat(r.getNameservers()).containsExactly("ns1.google.com");
    assertThat(r.isDnssec()).isFalse();
    assertThat(r.getMeta().getRdapServer()).isEqualTo("https://rdap.verisign.com/com/v1/");
    assertThat(r.getMeta().isCached()).isFalse();
  }

  @Test
  void parseDomainFollowResponse() throws Exception {
    DomainResponse r =
        RdapClient.MAPPER.readValue(Fixtures.domainFollowResponse(), DomainResponse.class);
    assertThat(r.getMeta().getFollowed()).isTrue();
    assertThat(r.getMeta().getRegistrarRdapServer())
        .isEqualTo("https://rdap.markmonitor.com/rdap/");
    assertThat(r.getEntities().getRegistrant()).isNotNull();
    assertThat(r.getEntities().getRegistrant().getName()).isEqualTo("Google LLC");
    assertThat(r.getEntities().getRegistrant().getCountryCode()).isEqualTo("US");
  }

  @Test
  void parseIpResponse() throws Exception {
    IpResponse r = RdapClient.MAPPER.readValue(Fixtures.ipResponse(), IpResponse.class);
    assertThat(r.getName()).isEqualTo("GOGL");
    assertThat(r.getCountry()).isEqualTo("US");
    assertThat(r.getCidr()).containsExactly("8.8.8.0/24");
    assertThat(r.getStartAddress()).isEqualTo("8.8.8.0");
    assertThat(r.getEndAddress()).isEqualTo("8.8.8.255");
    assertThat(r.getIpVersion()).isEqualTo("v4");
    assertThat(r.getRemarks()).hasSize(1);
    assertThat(r.getRemarks().get(0).getTitle()).isEqualTo("description");
    assertThat(r.getRemarks().get(0).getDescription()).isEqualTo("Google DNS");
    assertThat(r.getPort43()).isEqualTo("whois.arin.net");
  }

  @Test
  void parseAsnResponse() throws Exception {
    AsnResponse r = RdapClient.MAPPER.readValue(Fixtures.asnResponse(), AsnResponse.class);
    assertThat(r.getName()).isEqualTo("GOOGLE");
    assertThat(r.getHandle()).isEqualTo("AS15169");
    assertThat(r.getStartAutnum()).isEqualTo(15169);
    assertThat(r.getEndAutnum()).isEqualTo(15169);
    assertThat(r.getStatus()).containsExactly("active");
    assertThat(r.getType()).isNull();
    assertThat(r.getPort43()).isEqualTo("whois.arin.net");
  }

  @Test
  void parseNameserverResponse() throws Exception {
    NameserverResponse r =
        RdapClient.MAPPER.readValue(Fixtures.nameserverResponse(), NameserverResponse.class);
    assertThat(r.getLdhName()).isEqualTo("ns1.google.com");
    assertThat(r.getUnicodeName()).isNull();
    assertThat(r.getHandle()).isNull();
    assertThat(r.getIpAddresses().getV4()).containsExactly("216.239.32.10");
    assertThat(r.getIpAddresses().getV6()).containsExactly("2001:4860:4802:32::a");
    assertThat(r.getStatus()).isEmpty();
  }

  @Test
  void parseEntityResponse() throws Exception {
    EntityResponse r = RdapClient.MAPPER.readValue(Fixtures.entityResponse(), EntityResponse.class);
    assertThat(r.getHandle()).isEqualTo("GOGL");
    assertThat(r.getName()).isEqualTo("Google LLC");
    assertThat(r.getAddress()).isEqualTo("1600 Amphitheatre Parkway");
    assertThat(r.getPort43()).isEqualTo("whois.arin.net");
    assertThat(r.getPublicIds()).hasSize(1);
    assertThat(r.getPublicIds().get(0).getType()).isEqualTo("ARIN OrgID");
    assertThat(r.getPublicIds().get(0).getIdentifier()).isEqualTo("GOGL");
    assertThat(r.getEntities().getAbuse()).isNotNull();
    assertThat(r.getEntities().getAbuse().getEmail()).isEqualTo("network-abuse@google.com");
    assertThat(r.getAutnums()).hasSize(1);
    assertThat(r.getAutnums().get(0).getHandle()).isEqualTo("AS15169");
    assertThat(r.getAutnums().get(0).getStartAutnum()).isEqualTo(15169);
    assertThat(r.getNetworks()).hasSize(1);
    assertThat(r.getNetworks().get(0).getCidr()).containsExactly("8.8.8.0/24");
  }

  @Test
  void parseBulkDomainResponse() throws Exception {
    BulkDomainResponse r =
        RdapClient.MAPPER.readValue(Fixtures.bulkResponse(), BulkDomainResponse.class);
    assertThat(r.getSummary().getTotal()).isEqualTo(2);
    assertThat(r.getSummary().getSuccessful()).isEqualTo(1);
    assertThat(r.getSummary().getFailed()).isEqualTo(1);
    assertThat(r.getResults()).hasSize(2);

    BulkDomainResult success = r.getResults().get(0);
    assertThat(success.getDomain()).isEqualTo("google.com");
    assertThat(success.getStatus()).isEqualTo("success");
    assertThat(success.getData()).isNotNull();
    assertThat(success.getData().getDomain()).isEqualTo("google.com");

    BulkDomainResult failure = r.getResults().get(1);
    assertThat(failure.getDomain()).isEqualTo("invalid..com");
    assertThat(failure.getStatus()).isEqualTo("error");
    assertThat(failure.getError()).isEqualTo("invalid_domain");
    assertThat(failure.getMessage()).isEqualTo("The provided domain name is not valid.");
    assertThat(failure.getData()).isNull();
  }

  @Test
  void unknownFieldsIgnored() throws Exception {
    String json =
        "{\"domain\":\"test.com\",\"unknown_field\":\"value\","
            + "\"registrar\":{},\"dates\":{},\"entities\":{},\"meta\":{"
            + "\"rdap_server\":\"x\",\"raw_rdap_url\":\"x\",\"cached\":false,\"cache_expires\":\"x\"}}";
    DomainResponse r = RdapClient.MAPPER.readValue(json, DomainResponse.class);
    assertThat(r.getDomain()).isEqualTo("test.com");
  }

  @Test
  void emptyListFieldsDefaultToEmpty() throws Exception {
    String json =
        "{\"domain\":\"test.com\",\"registrar\":{},\"dates\":{},\"entities\":{},\"meta\":{"
            + "\"rdap_server\":\"x\",\"raw_rdap_url\":\"x\",\"cached\":false,\"cache_expires\":\"x\"}}";
    DomainResponse r = RdapClient.MAPPER.readValue(json, DomainResponse.class);
    assertThat(r.getStatus()).isEmpty();
    assertThat(r.getNameservers()).isEmpty();
  }
}
