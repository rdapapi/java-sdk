package io.rdapapi.client;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.rdapapi.client.exceptions.*;
import io.rdapapi.client.responses.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

public final class RdapClient implements AutoCloseable {

  private static final String DEFAULT_BASE_URL = "https://rdapapi.io/api/v1";
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

  static final ObjectMapper MAPPER =
      new ObjectMapper()
          .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
          .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  private final String baseUrl;
  private final HttpClient httpClient;
  private final String authHeader;
  private final String userAgent;

  public RdapClient(String apiKey) {
    this(apiKey, new RdapClientOptions());
  }

  public RdapClient(String apiKey, RdapClientOptions options) {
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalArgumentException("apiKey must be a non-empty string");
    }
    this.baseUrl = options.getBaseUrl() != null ? options.getBaseUrl() : DEFAULT_BASE_URL;
    Duration timeout = options.getTimeout() != null ? options.getTimeout() : DEFAULT_TIMEOUT;
    this.httpClient = HttpClient.newBuilder().connectTimeout(timeout).build();
    this.authHeader = "Bearer " + apiKey;
    this.userAgent = "rdapapi-java/" + Version.SDK;
  }

  public DomainResponse domain(String name) throws IOException, InterruptedException {
    return domain(name, new DomainOptions());
  }

  public DomainResponse domain(String name, DomainOptions options)
      throws IOException, InterruptedException {
    String path = "/domain/" + name;
    if (options.isFollow()) {
      path += "?follow=true";
    }
    byte[] body = doGet(path);
    return MAPPER.readValue(body, DomainResponse.class);
  }

  public IpResponse ip(String address) throws IOException, InterruptedException {
    byte[] body = doGet("/ip/" + address);
    return MAPPER.readValue(body, IpResponse.class);
  }

  public AsnResponse asn(int number) throws IOException, InterruptedException {
    return asn(String.valueOf(number));
  }

  public AsnResponse asn(String number) throws IOException, InterruptedException {
    String value = number.toUpperCase().replaceFirst("^AS", "");
    byte[] body = doGet("/asn/" + value);
    return MAPPER.readValue(body, AsnResponse.class);
  }

  public NameserverResponse nameserver(String host) throws IOException, InterruptedException {
    byte[] body = doGet("/nameserver/" + host);
    return MAPPER.readValue(body, NameserverResponse.class);
  }

  public EntityResponse entity(String handle) throws IOException, InterruptedException {
    byte[] body = doGet("/entity/" + handle);
    return MAPPER.readValue(body, EntityResponse.class);
  }

  public BulkDomainResponse bulkDomains(List<String> domains)
      throws IOException, InterruptedException {
    return bulkDomains(domains, new DomainOptions());
  }

  public BulkDomainResponse bulkDomains(List<String> domains, DomainOptions options)
      throws IOException, InterruptedException {
    ObjectNode root = MAPPER.createObjectNode();
    var domainsNode = root.putArray("domains");
    for (String d : domains) {
      domainsNode.add(d);
    }
    if (options.isFollow()) {
      root.put("follow", true);
    }

    byte[] responseBody = doPost("/domains/bulk", MAPPER.writeValueAsBytes(root));

    JsonNode tree = MAPPER.readTree(responseBody);
    JsonNode results = tree.get("results");
    if (results != null && results.isArray()) {
      for (JsonNode result : results) {
        if ("success".equals(result.path("status").asText())
            && result.has("data")
            && result.has("meta")) {
          ((ObjectNode) result.get("data")).set("meta", result.get("meta"));
          ((ObjectNode) result).remove("meta");
        }
      }
    }

    return MAPPER.treeToValue(tree, BulkDomainResponse.class);
  }

  @Override
  public void close() {
    // java.net.http.HttpClient does not require explicit cleanup.
  }

  private byte[] doGet(String path) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Authorization", authHeader)
            .header("User-Agent", userAgent)
            .header("Accept", "application/json")
            .GET()
            .build();
    return doRequest(request);
  }

  private byte[] doPost(String path, byte[] body) throws IOException, InterruptedException {
    HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Authorization", authHeader)
            .header("User-Agent", userAgent)
            .header("Accept", "application/json")
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofByteArray(body))
            .build();
    return doRequest(request);
  }

  private byte[] doRequest(HttpRequest request) throws IOException, InterruptedException {
    HttpResponse<byte[]> response =
        httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

    if (response.statusCode() >= 400) {
      handleError(response);
    }

    return response.body();
  }

  private void handleError(HttpResponse<byte[]> response) {
    String errorCode = "unknown_error";
    String message = "HTTP " + response.statusCode();
    try {
      JsonNode body = MAPPER.readTree(response.body());
      if (body.has("error")) {
        errorCode = body.get("error").asText();
      }
      if (body.has("message")) {
        message = body.get("message").asText();
      }
    } catch (Exception ignored) {
      // non-JSON error body — use defaults
    }

    Integer retryAfter = null;
    if (response.statusCode() == 429 || response.statusCode() == 503) {
      String retryHeader = response.headers().firstValue("Retry-After").orElse(null);
      if (retryHeader != null) {
        try {
          retryAfter = Integer.parseInt(retryHeader);
        } catch (NumberFormatException ignored) {
          // invalid Retry-After header — leave as null
        }
      }
    }

    throw createException(response.statusCode(), errorCode, message, retryAfter);
  }

  private RdapApiException createException(
      int statusCode, String errorCode, String message, Integer retryAfter) {
    switch (statusCode) {
      case 400:
        return new ValidationException(message, errorCode);
      case 401:
        return new AuthenticationException(message, errorCode);
      case 403:
        return new SubscriptionRequiredException(message, errorCode);
      case 404:
        return new NotFoundException(message, errorCode);
      case 429:
        return new RateLimitException(message, errorCode, retryAfter);
      case 502:
        return new UpstreamException(message, errorCode);
      case 503:
        return new TemporarilyUnavailableException(message, errorCode, retryAfter);
      default:
        return new RdapApiException(message, statusCode, errorCode);
    }
  }
}
