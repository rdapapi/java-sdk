package io.rdapapi.client;

import static org.assertj.core.api.Assertions.*;

import io.rdapapi.client.exceptions.*;
import io.rdapapi.client.responses.*;
import java.io.IOException;
import java.time.Duration;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RdapClientTest {

  private MockWebServer server;

  @BeforeEach
  void setUp() throws IOException {
    server = new MockWebServer();
    server.start();
  }

  @AfterEach
  void tearDown() throws IOException {
    server.shutdown();
  }

  private RdapClient createClient() {
    return new RdapClient(
        "test-api-key", new RdapClientOptions().baseUrl(server.url("/api/v1").toString()));
  }

  // --- Constructor ---

  @Test
  void constructorRejectsNullApiKey() {
    assertThatThrownBy(() -> new RdapClient(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("apiKey must be a non-empty string");
  }

  @Test
  void singleArgConstructorWorks() {
    RdapClient client = new RdapClient("api-key");
    assertThat(client).isNotNull();
    client.close();
  }

  @Test
  void constructorRejectsEmptyApiKey() {
    assertThatThrownBy(() -> new RdapClient(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("apiKey must be a non-empty string");
  }

  @Test
  void customOptions() {
    RdapClient client =
        new RdapClient(
            "key",
            new RdapClientOptions()
                .baseUrl("https://custom.api.com/v1")
                .timeout(Duration.ofSeconds(10)));
    assertThat(client).isNotNull();
    client.close();
  }

  // --- Request headers ---

  @Test
  void sendsCorrectHeaders() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.domainResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.domain("google.com");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getHeader("Authorization")).isEqualTo("Bearer test-api-key");
    assertThat(request.getHeader("User-Agent")).isEqualTo("rdapapi-java/" + Version.SDK);
    assertThat(request.getHeader("Accept")).isEqualTo("application/json");
    client.close();
  }

  // --- Domain lookup ---

  @Test
  void domainLookup() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.domainResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    DomainResponse r = client.domain("google.com");

    assertThat(r.getDomain()).isEqualTo("google.com");
    assertThat(r.getRegistrar().getName()).isEqualTo("MarkMonitor Inc.");
    assertThat(r.getDates().getRegistered()).isEqualTo("1997-09-15T04:00:00Z");
    assertThat(r.getNameservers()).containsExactly("ns1.google.com");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/domain/google.com");
    assertThat(request.getMethod()).isEqualTo("GET");
    client.close();
  }

  @Test
  void domainLookupWithFollow() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.domainFollowResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    DomainResponse r = client.domain("google.com", new DomainOptions().follow(true));

    assertThat(r.getMeta().getFollowed()).isTrue();
    assertThat(r.getEntities().getRegistrant().getName()).isEqualTo("Google LLC");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/domain/google.com?follow=true");
    client.close();
  }

  @Test
  void domainLookupRefusingTheWhoisFallback() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.domainResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.domain("example.it", new DomainOptions().whois(false));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/domain/example.it?whois=false");
    client.close();
  }

  @Test
  void domainLookupWithFollowAndWhoisRefused() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.domainResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.domain("example.it", new DomainOptions().follow(true).whois(false));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/domain/example.it?follow=true&whois=false");
    client.close();
  }

  @Test
  void domainLookupOverWhoisFallback() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.whoisDomainResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    DomainResponse r = client.domain("example.it");

    assertThat(r.getMeta().getSource()).isEqualTo("whois");
    assertThat(r.getMeta().getServer()).isEqualTo("whois.nic.it");
    assertThat(r.getDnssec()).isNull();
    client.close();
  }

  @Test
  void domainLookupWithoutFollow() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.domainResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.domain("google.com", new DomainOptions().follow(false));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/domain/google.com");
    client.close();
  }

  // --- Ping ---

  @Test
  void ping() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.pingResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();

    assertThat(client.ping().getStatus()).isEqualTo("ok");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/ping");
    client.close();
  }

  // --- IP lookup ---

  @Test
  void ipLookup() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.ipResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    IpResponse r = client.ip("8.8.8.8");

    assertThat(r.getName()).isEqualTo("GOGL");
    assertThat(r.getCountry()).isEqualTo("US");
    assertThat(r.getCidr()).containsExactly("8.8.8.0/24");
    assertThat(r.getGeofeed()).isEqualTo("https://geofeed.example.net/geofeed.csv");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/ip/8.8.8.8");
    client.close();
  }

  // --- ASN lookup ---

  @Test
  void asnLookupWithInt() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.asnResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    AsnResponse r = client.asn(15169);

    assertThat(r.getName()).isEqualTo("GOOGLE");
    assertThat(r.getStartAutnum()).isEqualTo(15169);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/asn/15169");
    client.close();
  }

  @Test
  void asnLookupWithStringPrefix() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.asnResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.asn("AS15169");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/asn/15169");
    client.close();
  }

  @Test
  void asnLookupWithLowercasePrefix() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.asnResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.asn("as15169");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/asn/15169");
    client.close();
  }

  @Test
  void asnLookupWithPlainString() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.asnResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.asn("15169");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/asn/15169");
    client.close();
  }

  // --- Nameserver lookup ---

  @Test
  void nameserverLookup() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.nameserverResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    NameserverResponse r = client.nameserver("ns1.google.com");

    assertThat(r.getLdhName()).isEqualTo("ns1.google.com");
    assertThat(r.getIpAddresses().getV4()).containsExactly("216.239.32.10");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/nameserver/ns1.google.com");
    client.close();
  }

  // --- Entity lookup ---

  @Test
  void entityLookup() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.entityResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    EntityResponse r = client.entity("GOGL");

    assertThat(r.getHandle()).isEqualTo("GOGL");
    assertThat(r.getName()).isEqualTo("Google LLC");
    assertThat(r.getAutnums()).hasSize(1);
    assertThat(r.getNetworks()).hasSize(1);

    RecordedRequest request = server.takeRequest();
    assertThat(request.getPath()).isEqualTo("/api/v1/entity/GOGL");
    client.close();
  }

  // --- Bulk domain lookup ---

  @Test
  void bulkDomainLookup() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.bulkResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    BulkDomainResponse r = client.bulkDomains(Arrays.asList("google.com", "invalid..com"));

    assertThat(r.getSummary().getTotal()).isEqualTo(3);
    assertThat(r.getSummary().getSuccessful()).isEqualTo(1);
    assertThat(r.getSummary().getFailed()).isEqualTo(2);
    assertThat(r.getResults()).hasSize(3);

    BulkDomainResult success = r.getResults().get(0);
    assertThat(success.getStatus()).isEqualTo("success");
    assertThat(success.getData().getDomain()).isEqualTo("google.com");
    assertThat(success.getData().getMeta()).isNotNull();
    assertThat(success.getData().getMeta().getServer()).isEqualTo("rdap.verisign.com");
    assertThat(success.getData().getMeta().getSource()).isEqualTo("rdap");
    assertThat(success.getMeta().getServer()).isEqualTo("rdap.verisign.com");

    BulkDomainResult failure = r.getResults().get(1);
    assertThat(failure.getStatus()).isEqualTo("error");
    assertThat(failure.getError()).isEqualTo("invalid_domain");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getMethod()).isEqualTo("POST");
    assertThat(request.getPath()).isEqualTo("/api/v1/domains/bulk");
    assertThat(request.getHeader("Content-Type")).isEqualTo("application/json");
    client.close();
  }

  @Test
  void bulkDomainLookupWithFollow() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.bulkResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.bulkDomains(
        Arrays.asList("google.com", "invalid..com"), new DomainOptions().follow(true));

    RecordedRequest request = server.takeRequest();
    String body = request.getBody().readUtf8();
    assertThat(body).contains("\"follow\":true");
    client.close();
  }

  @Test
  void bulkDomainLookupRefusingTheWhoisFallback() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.bulkResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.bulkDomains(Arrays.asList("google.com"), new DomainOptions().whois(false));

    RecordedRequest request = server.takeRequest();
    assertThat(request.getBody().readUtf8()).contains("\"whois\":false");
    client.close();
  }

  @Test
  void bulkDomainLookupWithoutFollow() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.bulkResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();
    client.bulkDomains(Arrays.asList("google.com"));

    RecordedRequest request = server.takeRequest();
    String body = request.getBody().readUtf8();
    assertThat(body).doesNotContain("follow");
    assertThat(body).doesNotContain("whois");
    client.close();
  }

  // --- Error handling ---

  @Test
  void error400ThrowsValidationException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(400)
            .setBody(
                Fixtures.errorResponse(
                    "invalid_domain", "The provided domain name is not valid.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("bad"))
        .isInstanceOf(ValidationException.class)
        .satisfies(
            ex -> {
              ValidationException e = (ValidationException) ex;
              assertThat(e.getStatusCode()).isEqualTo(400);
              assertThat(e.getErrorCode()).isEqualTo("invalid_domain");
              assertThat(e.getMessage()).isEqualTo("The provided domain name is not valid.");
            });
    client.close();
  }

  @Test
  void error401ThrowsAuthenticationException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(401)
            .setBody(Fixtures.errorResponse("unauthenticated", "Invalid or missing API token.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com")).isInstanceOf(AuthenticationException.class);
    client.close();
  }

  @Test
  void error403ThrowsSubscriptionRequiredException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(403)
            .setBody(
                Fixtures.errorResponse(
                    "subscription_required", "An active subscription is required.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(SubscriptionRequiredException.class);
    client.close();
  }

  @Test
  void error404ThrowsNotFoundException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(404)
            .setBody(Fixtures.errorResponse("not_found", "No RDAP data found.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("nope.example"))
        .isInstanceOf(NotFoundException.class)
        .isNotInstanceOf(NotSupportedException.class);
    client.close();
  }

  @Test
  void error404WithNotSupportedCodeThrowsNotSupportedException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(404)
            .setBody(Fixtures.errorResponse("not_supported", "The TLD '.nope' is not supported.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("example.nope"))
        .isInstanceOf(NotSupportedException.class)
        // Backwards compatible: NotSupportedException extends NotFoundException.
        .isInstanceOf(NotFoundException.class)
        .satisfies(
            ex -> {
              NotSupportedException e = (NotSupportedException) ex;
              assertThat(e.getErrorCode()).isEqualTo("not_supported");
              assertThat(e.getStatusCode()).isEqualTo(404);
            });
    client.close();
  }

  @Test
  void notSupportedRoutingAppliesToIpLookupToo() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(404)
            .setBody(Fixtures.errorResponse("not_supported", "No RIR covers this IP range.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.ip("203.0.113.1")).isInstanceOf(NotSupportedException.class);
    client.close();
  }

  @Test
  void error422ThrowsRequestFailedExceptionWithFieldErrors() {
    server.enqueue(
        new MockResponse().setResponseCode(422).setBody(Fixtures.validationErrorResponse()));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.bulkDomains(Collections.singletonList("test.com")))
        .isInstanceOf(RequestFailedException.class)
        .satisfies(
            ex -> {
              RequestFailedException e = (RequestFailedException) ex;
              assertThat(e.getStatusCode()).isEqualTo(422);
              assertThat(e.getErrorCode()).isEqualTo("request_failed");
              assertThat(e.getErrors())
                  .containsOnlyKeys("domains")
                  .satisfies(
                      errors ->
                          assertThat(errors.get("domains"))
                              .containsExactly(
                                  "The domains field must not have more than 10 items."));
            });
    client.close();
  }

  @Test
  void error422KeepsAScalarMessageInsteadOfDroppingIt() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(422)
            .setBody(Fixtures.validationErrorWithScalarMessage()));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.bulkDomains(Collections.singletonList("test.com")))
        .isInstanceOf(RequestFailedException.class)
        .satisfies(
            ex ->
                assertThat(((RequestFailedException) ex).getErrors().get("domains"))
                    .containsExactly("The domains field is required."));
    client.close();
  }

  @Test
  void error422WithoutFieldErrors() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(422)
            .setBody(Fixtures.errorResponse("request_failed", "The body failed validation.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.bulkDomains(Collections.singletonList("test.com")))
        .isInstanceOf(RequestFailedException.class)
        .satisfies(ex -> assertThat(((RequestFailedException) ex).getErrors()).isEmpty());
    client.close();
  }

  @Test
  void error429ThrowsRateLimitExceptionWithRetryAfter() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "60")
            .setBody(Fixtures.errorResponse("rate_limit_exceeded", "Rate limit exceeded.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(
            ex -> {
              RateLimitException e = (RateLimitException) ex;
              assertThat(e.getRetryAfter()).isEqualTo(60);
              assertThat(e.getStatusCode()).isEqualTo(429);
            });
    client.close();
  }

  @Test
  void error429WithoutRetryAfterHeader() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setBody(Fixtures.errorResponse("rate_limit_exceeded", "Rate limit exceeded.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(
            ex -> {
              RateLimitException e = (RateLimitException) ex;
              assertThat(e.getRetryAfter()).isNull();
            });
    client.close();
  }

  @Test
  void error429ReadsRetryAfterFromTheBodyWhenTheHeaderIsMissing() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setBody(
                "{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded.\","
                    + "\"retry_after\":30}"));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(ex -> assertThat(((RateLimitException) ex).getRetryAfter()).isEqualTo(30));
    client.close();
  }

  @Test
  void httpDateRetryAfterHeaderWinsOverTheBody() {
    String inTenMinutes =
        DateTimeFormatter.RFC_1123_DATE_TIME.format(
            ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(10));
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", inTenMinutes)
            .setBody(
                "{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded.\","
                    + "\"retry_after\":45}"));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(
            ex ->
                assertThat(((RateLimitException) ex).getRetryAfter())
                    .isNotNull()
                    .isBetween(540, 600));
    client.close();
  }

  @Test
  void httpDateRetryAfterAlreadyPastClampsToZero() {
    String anHourAgo =
        DateTimeFormatter.RFC_1123_DATE_TIME.format(
            ZonedDateTime.now(ZoneOffset.UTC).minusHours(1));
    server.enqueue(
        new MockResponse()
            .setResponseCode(503)
            .setHeader("Retry-After", anHourAgo)
            .setBody(
                Fixtures.errorResponse(
                    "temporarily_unavailable",
                    "Data for this domain is temporarily unavailable.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(TemporarilyUnavailableException.class)
        .satisfies(
            ex -> assertThat(((TemporarilyUnavailableException) ex).getRetryAfter()).isZero());
    client.close();
  }

  @Test
  void zeroRetryAfterHeaderMeansRetryNowAndNotAbsent() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "0")
            .setBody(
                "{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded.\","
                    + "\"retry_after\":45}"));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(ex -> assertThat(((RateLimitException) ex).getRetryAfter()).isZero());
    client.close();
  }

  @Test
  void absurdlyLargeRetryAfterHeaderClampsInsteadOfOverflowing() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "99999999999999")
            .setBody(Fixtures.errorResponse("rate_limit_exceeded", "Rate limit exceeded.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(
            ex ->
                assertThat(((RateLimitException) ex).getRetryAfter()).isEqualTo(Integer.MAX_VALUE));
    client.close();
  }

  @Test
  void unparseableRetryAfterHeaderFallsBackToTheBody() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "soon")
            .setBody(
                "{\"error\":\"rate_limit_exceeded\",\"message\":\"Rate limit exceeded.\","
                    + "\"retry_after\":45}"));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(ex -> assertThat(((RateLimitException) ex).getRetryAfter()).isEqualTo(45));
    client.close();
  }

  @Test
  void unparseableRetryAfterHeaderAndNoBodyValueReadsAsUnset() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(429)
            .setHeader("Retry-After", "soon")
            .setBody(Fixtures.errorResponse("rate_limit_exceeded", "Rate limit exceeded.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RateLimitException.class)
        .satisfies(ex -> assertThat(((RateLimitException) ex).getRetryAfter()).isNull());
    client.close();
  }

  @Test
  void error502ThrowsUpstreamException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(502)
            .setHeader("Retry-After", "60")
            .setBody(Fixtures.errorResponse("lookup_failed", "RDAP lookup failed.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(UpstreamException.class)
        .satisfies(ex -> assertThat(((UpstreamException) ex).getRetryAfter()).isEqualTo(60));
    client.close();
  }

  @Test
  void error502WithoutRetryAfter() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(502)
            .setBody(Fixtures.errorResponse("bad_gateway", "The API is temporarily unavailable.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(UpstreamException.class)
        .satisfies(ex -> assertThat(((UpstreamException) ex).getRetryAfter()).isNull());
    client.close();
  }

  @Test
  void error503ThrowsTemporarilyUnavailableExceptionWithRetryAfter() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(503)
            .setHeader("Retry-After", "300")
            .setBody(
                Fixtures.errorResponse(
                    "temporarily_unavailable",
                    "Data for this domain is temporarily unavailable.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(TemporarilyUnavailableException.class)
        .satisfies(
            ex -> {
              TemporarilyUnavailableException e = (TemporarilyUnavailableException) ex;
              assertThat(e.getRetryAfter()).isEqualTo(300);
              assertThat(e.getStatusCode()).isEqualTo(503);
            });
    client.close();
  }

  @Test
  void error503WithoutRetryAfterHeader() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(503)
            .setBody(
                Fixtures.errorResponse(
                    "temporarily_unavailable",
                    "Data for this domain is temporarily unavailable.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(TemporarilyUnavailableException.class)
        .satisfies(
            ex -> {
              TemporarilyUnavailableException e = (TemporarilyUnavailableException) ex;
              assertThat(e.getRetryAfter()).isNull();
            });
    client.close();
  }

  @Test
  void unknownErrorThrowsBaseException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(500)
            .setBody(Fixtures.errorResponse("server_error", "Internal server error")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RdapApiException.class)
        .isNotInstanceOf(ValidationException.class)
        .isNotInstanceOf(AuthenticationException.class)
        .isNotInstanceOf(NotFoundException.class)
        .satisfies(
            ex -> {
              RdapApiException e = (RdapApiException) ex;
              assertThat(e.getStatusCode()).isEqualTo(500);
              assertThat(e.getErrorCode()).isEqualTo("server_error");
            });
    client.close();
  }

  @Test
  void nonJsonErrorBody() {
    server.enqueue(new MockResponse().setResponseCode(500).setBody("Internal Server Error"));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com"))
        .isInstanceOf(RdapApiException.class)
        .satisfies(
            ex -> {
              RdapApiException e = (RdapApiException) ex;
              assertThat(e.getErrorCode()).isEqualTo("unknown_error");
              assertThat(e.getMessage()).isEqualTo("HTTP 500");
            });
    client.close();
  }

  @Test
  void postErrorHandling() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(403)
            .setBody(
                Fixtures.errorResponse(
                    "plan_upgrade_required", "Bulk lookups require a Pro or Business plan.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.bulkDomains(Collections.singletonList("test.com")))
        .isInstanceOf(SubscriptionRequiredException.class);
    client.close();
  }

  // --- TLDs ---

  @Test
  void tldsListReturnsEntriesWithEtag() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.tldsResponse())
            .setHeader("Content-Type", "application/json")
            .setHeader("ETag", "\"abc\""));
    RdapClient client = createClient();

    TldListResponse result = client.tlds();

    assertThat(result).isNotNull();
    assertThat(result.getMeta().getCount()).isEqualTo(3);
    assertThat(result.getMeta().getCoverage()).isEqualTo(0.5);
    assertThat(result.getMeta().getThresholds().getAlways()).isEqualTo(0.99);
    assertThat(result.getData()).hasSize(3);
    assertThat(result.getData().get(0).getTld()).isEqualTo("com");
    assertThat(result.getData().get(0).getServer()).isEqualTo("rdap.verisign.com");
    assertThat(result.getData().get(2).getProtocol()).isEqualTo("whois");
    assertThat(result.getData().get(0).getFieldAvailability()).isNotNull();
    assertThat(result.getData().get(0).getFieldAvailability().getRegisteredAt())
        .isEqualTo(AvailabilityLevel.ALWAYS);
    assertThat(result.getData().get(1).getFieldAvailability()).isNull();
    assertThat(result.getEtag()).isEqualTo("\"abc\"");
    client.close();
  }

  @Test
  void tldsForwardsSinceAndServerQueryParams() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.tldsResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();

    client.tlds(new TldsOptions().since("2026-04-01T00:00:00Z").server("rdap.verisign.com"));

    RecordedRequest request = server.takeRequest();
    String path = request.getPath();
    assertThat(path).contains("since=2026-04-01T00%3A00%3A00Z");
    assertThat(path).contains("server=rdap.verisign.com");
    client.close();
  }

  @Test
  void tldsReturnsNullOnNotModified() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(304));
    RdapClient client = createClient();

    TldListResponse result = client.tlds(new TldsOptions().ifNoneMatch("\"abc\""));

    assertThat(result).isNull();
    RecordedRequest request = server.takeRequest();
    assertThat(request.getHeader("If-None-Match")).isEqualTo("\"abc\"");
    client.close();
  }

  @Test
  void tldsRaisesTypedErrorOnFailure() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(401)
            .setBody(Fixtures.errorResponse("unauthenticated", "Invalid API token.")));
    RdapClient client = createClient();

    assertThatThrownBy(client::tlds).isInstanceOf(AuthenticationException.class);
    client.close();
  }

  @Test
  void tldsNullEtagWhenServerOmitsHeader() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.tldsResponse())
            .setHeader("Content-Type", "application/json"));
    RdapClient client = createClient();

    TldListResponse result = client.tlds();

    assertThat(result.getEtag()).isNull();
    client.close();
  }

  @Test
  void tldReturnsSingleEntry() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.tldResponse())
            .setHeader("Content-Type", "application/json")
            .setHeader("ETag", "\"com-1\""));
    RdapClient client = createClient();

    TldResponse result = client.tld("com");

    assertThat(result.getData().getTld()).isEqualTo("com");
    assertThat(result.getMeta().getThresholds().getUsually()).isEqualTo(0.8);
    assertThat(result.getEtag()).isEqualTo("\"com-1\"");
    client.close();
  }

  @Test
  void tldReturnsNullOnNotModified() throws Exception {
    server.enqueue(new MockResponse().setResponseCode(304));
    RdapClient client = createClient();

    TldResponse result = client.tld("com", new TldOptions().ifNoneMatch("\"com-1\""));

    assertThat(result).isNull();
    client.close();
  }

  @Test
  void tldThrowsNotFoundForUnknownTld() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(404)
            .setBody(
                Fixtures.errorResponse(
                    "not_found", "No RDAP server is registered for the TLD 'nope'.")));
    RdapClient client = createClient();

    assertThatThrownBy(() -> client.tld("nope")).isInstanceOf(NotFoundException.class);
    client.close();
  }

  // --- Version ---

  @Test
  void versionIsDefined() {
    assertThat(Version.SDK).isNotNull().isNotEmpty();
  }

  // --- AutoCloseable ---

  @Test
  void tryWithResources() throws Exception {
    server.enqueue(
        new MockResponse()
            .setBody(Fixtures.domainResponse())
            .setHeader("Content-Type", "application/json"));
    try (RdapClient client = createClient()) {
      DomainResponse r = client.domain("google.com");
      assertThat(r.getDomain()).isEqualTo("google.com");
    }
  }
}
