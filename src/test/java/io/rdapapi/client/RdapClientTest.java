package io.rdapapi.client;

import static org.assertj.core.api.Assertions.*;

import io.rdapapi.client.exceptions.*;
import io.rdapapi.client.responses.*;
import java.io.IOException;
import java.time.Duration;
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

    assertThat(r.getSummary().getTotal()).isEqualTo(2);
    assertThat(r.getSummary().getSuccessful()).isEqualTo(1);
    assertThat(r.getSummary().getFailed()).isEqualTo(1);
    assertThat(r.getResults()).hasSize(2);

    BulkDomainResult success = r.getResults().get(0);
    assertThat(success.getStatus()).isEqualTo("success");
    assertThat(success.getData().getDomain()).isEqualTo("google.com");
    assertThat(success.getData().getMeta()).isNotNull();
    assertThat(success.getData().getMeta().getRdapServer())
        .isEqualTo("https://rdap.verisign.com/com/v1/");

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
    assertThatThrownBy(() -> client.domain("nope.example")).isInstanceOf(NotFoundException.class);
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
  void error502ThrowsUpstreamException() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(502)
            .setBody(Fixtures.errorResponse("lookup_failed", "RDAP lookup failed.")));
    RdapClient client = createClient();
    assertThatThrownBy(() -> client.domain("test.com")).isInstanceOf(UpstreamException.class);
    client.close();
  }

  @Test
  void error503ThrowsTemporarilyUnavailableExceptionWithRetryAfter() {
    server.enqueue(
        new MockResponse()
            .setResponseCode(503)
            .setHeader("Retry-After", "300")
            .setBody(Fixtures.errorResponse("temporarily_unavailable", "Data for this domain is temporarily unavailable.")));
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
            .setBody(Fixtures.errorResponse("temporarily_unavailable", "Data for this domain is temporarily unavailable.")));
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
