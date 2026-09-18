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
    assertThat(r.getDnssec()).isFalse();
    assertThat(r.getRedacted()).isNull();
    assertThat(r.getMeta().getServer()).isEqualTo("rdap.verisign.com");
    assertThat(r.getMeta().getSource()).isEqualTo("rdap");
    assertThat(r.getMeta().getRdapServer()).isEqualTo("https://rdap.verisign.com/com/v1/");
    assertThat(r.getMeta().getCached()).isFalse();
  }

  @Test
  void parseWhoisDomainResponse() throws Exception {
    DomainResponse r =
        RdapClient.MAPPER.readValue(Fixtures.whoisDomainResponse(), DomainResponse.class);
    assertThat(r.getDomain()).isEqualTo("example.it");
    assertThat(r.getDnssec()).isNull();
    assertThat(r.getMeta().getSource()).isEqualTo("whois");
    assertThat(r.getMeta().getServer()).isEqualTo("whois.nic.it");
    assertThat(r.getMeta().getRawRdapUrl()).isNull();
  }

  @Test
  void parseRedaction() throws Exception {
    DomainResponse r =
        RdapClient.MAPPER.readValue(Fixtures.domainFollowResponse(), DomainResponse.class);
    Redaction redacted = r.getRedacted();
    assertThat(redacted).isNotNull();
    assertThat(redacted.getHandle()).isEqualTo(RedactionMethod.REPLACEMENT_VALUE);
    assertThat(redacted.getRegistrar())
        .containsExactly(entry("iana_id", RedactionMethod.REPLACEMENT_VALUE));
    assertThat(redacted.getEntities()).containsOnlyKeys("registrant");
    assertThat(redacted.getEntities().get("registrant"))
        .containsExactly(
            entry("email", RedactionMethod.REMOVAL), entry("name", RedactionMethod.EMPTY_VALUE));
  }

  @Test
  void redactionPassesAnUnknownMethodThrough() throws Exception {
    String json = "{\"entities\":{\"registrant\":{\"name\":\"someFutureMethod\"}}}";
    Redaction redacted = RdapClient.MAPPER.readValue(json, Redaction.class);
    assertThat(redacted.getHandle()).isNull();
    assertThat(redacted.getRegistrar()).isEmpty();
    assertThat(redacted.getEntities().get("registrant").get("name")).isEqualTo("someFutureMethod");
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
    assertThat(r.getGeofeed()).isEqualTo("https://geofeed.example.net/geofeed.csv");
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
    assertThat(r.getCountry()).isEqualTo("US");
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
    assertThat(r.getSummary().getTotal()).isEqualTo(3);
    assertThat(r.getSummary().getSuccessful()).isEqualTo(1);
    assertThat(r.getSummary().getFailed()).isEqualTo(2);
    assertThat(r.getResults()).hasSize(3);

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
    assertThat(failure.getMeta()).isNull();

    BulkDomainResult notFound = r.getResults().get(2);
    assertThat(notFound.getError()).isEqualTo("not_found");
    assertThat(notFound.getMeta().getServer()).isEqualTo("rdap.verisign.com");
    assertThat(notFound.getMeta().getSource()).isEqualTo("rdap");
    // The API omits cache state from a failed entry's partial meta: null, not "served live".
    assertThat(notFound.getMeta().getCached()).isNull();
    assertThat(notFound.getMeta().getCacheExpires()).isNull();
  }

  @Test
  void datesConvenienceMethods() throws Exception {
    DomainResponse r = RdapClient.MAPPER.readValue(Fixtures.domainResponse(), DomainResponse.class);
    assertThat(r.getDates().getRegisteredAt()).isNotNull();
    assertThat(r.getDates().getRegisteredAt().toString()).isEqualTo("1997-09-15T04:00:00Z");
    assertThat(r.getDates().getExpiresAt()).isNotNull();
    assertThat(r.getDates().getExpiresInDays()).isGreaterThan(0);
    assertThat(r.getDates().getUpdatedAt()).isNull();
  }

  @Test
  void datesNullFieldsReturnNull() throws Exception {
    String json =
        "{\"domain\":\"test.com\",\"registrar\":{},\"dates\":{},\"entities\":{},\"meta\":{"
            + "\"rdap_server\":\"x\",\"raw_rdap_url\":\"x\",\"cached\":false,\"cache_expires\":\"x\"}}";
    DomainResponse r = RdapClient.MAPPER.readValue(json, DomainResponse.class);
    assertThat(r.getDates().getRegisteredAt()).isNull();
    assertThat(r.getDates().getExpiresAt()).isNull();
    assertThat(r.getDates().getExpiresInDays()).isNull();
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
  void parseTldListResponse() throws Exception {
    TldListResponse r = RdapClient.MAPPER.readValue(Fixtures.tldsResponse(), TldListResponse.class);
    assertThat(r.getMeta().getCount()).isEqualTo(3);
    assertThat(r.getMeta().getCoverage()).isEqualTo(0.5);
    assertThat(r.getMeta().getComputedAt()).isEqualTo("2026-04-22T10:00:00Z");
    assertThat(r.getMeta().getThresholds().getAlways()).isEqualTo(0.99);
    assertThat(r.getMeta().getThresholds().getUsually()).isEqualTo(0.8);
    assertThat(r.getMeta().getThresholds().getSometimes()).isEqualTo(0.0);
    assertThat(r.getData()).hasSize(3);
    assertThat(r.getData().get(0).getTld()).isEqualTo("com");
    assertThat(r.getData().get(0).getProtocol()).isEqualTo("rdap");
    assertThat(r.getData().get(0).getSupportedSince()).isEqualTo("2026-03-07T00:00:00Z");
    assertThat(r.getData().get(0).getServer()).isEqualTo("rdap.verisign.com");
    assertThat(r.getData().get(0).getRdapServerUrl())
        .isEqualTo("https://rdap.verisign.com/com/v1/");
    assertThat(r.getData().get(0).getFieldAvailability()).isNotNull();
    assertThat(r.getData().get(0).getFieldAvailability().getRegistrar())
        .isEqualTo(AvailabilityLevel.SOMETIMES);
    assertThat(r.getData().get(0).getFieldAvailability().getRegisteredAt())
        .isEqualTo(AvailabilityLevel.ALWAYS);
    assertThat(r.getData().get(0).getFieldAvailability().getExpiresAt())
        .isEqualTo(AvailabilityLevel.ALWAYS);
    assertThat(r.getData().get(0).getFieldAvailability().getNameservers())
        .isEqualTo(AvailabilityLevel.ALWAYS);
    assertThat(r.getData().get(0).getFieldAvailability().getStatus())
        .isEqualTo(AvailabilityLevel.ALWAYS);
    assertThat(r.getData().get(1).getFieldAvailability()).isNull();

    TldEntry whoisServed = r.getData().get(2);
    assertThat(whoisServed.getTld()).isEqualTo("it");
    assertThat(whoisServed.getProtocol()).isEqualTo("whois");
    assertThat(whoisServed.getServer()).isEqualTo("whois.nic.it");
    assertThat(whoisServed.getRdapServerHost()).isNull();
    assertThat(whoisServed.getRdapServerUrl()).isNull();
    assertThat(whoisServed.getFieldAvailability()).isNull();
  }

  @Test
  void parseTldResponse() throws Exception {
    TldResponse r = RdapClient.MAPPER.readValue(Fixtures.tldResponse(), TldResponse.class);
    assertThat(r.getData().getTld()).isEqualTo("com");
    assertThat(r.getData().getServer()).isEqualTo("rdap.verisign.com");
    assertThat(r.getMeta().getComputedAt()).isEqualTo("2026-04-22T10:00:00Z");
    assertThat(r.getMeta().getThresholds().getAlways()).isEqualTo(0.99);
  }

  @Test
  void parsePingResponse() throws Exception {
    PingResponse r = RdapClient.MAPPER.readValue(Fixtures.pingResponse(), PingResponse.class);
    assertThat(r.getStatus()).isEqualTo("ok");
  }

  @Test
  void tldListResponseEmptyDataDefaultsToEmptyList() throws Exception {
    String json =
        "{\"meta\":{\"computed_at\":\"\",\"count\":0,\"coverage\":0,"
            + "\"thresholds\":{\"always\":0,\"usually\":0,\"sometimes\":0}}}";
    TldListResponse r = RdapClient.MAPPER.readValue(json, TldListResponse.class);
    assertThat(r.getData()).isEmpty();
  }

  @Test
  void availabilityLevelRejectsUnknownValue() {
    assertThatThrownBy(() -> AvailabilityLevel.fromWire("bogus"))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void availabilityLevelRoundTripsWireValue() {
    assertThat(AvailabilityLevel.ALWAYS.toWire()).isEqualTo("always");
    assertThat(AvailabilityLevel.USUALLY.toWire()).isEqualTo("usually");
    assertThat(AvailabilityLevel.SOMETIMES.toWire()).isEqualTo("sometimes");
    assertThat(AvailabilityLevel.NEVER.toWire()).isEqualTo("never");
    assertThat(AvailabilityLevel.fromWire("never")).isEqualTo(AvailabilityLevel.NEVER);
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
