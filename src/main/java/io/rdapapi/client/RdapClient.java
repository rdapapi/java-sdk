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

  public TldListResponse tlds() throws IOException, InterruptedException {
    return tlds(new TldsOptions());
  }

  /**
   * List every TLD the API can resolve via RDAP.
   *
   * <p>Does not count against the monthly quota. Returns {@code null} when {@link
   * TldsOptions#ifNoneMatch(String)} is provided and matches the server's current ETag (HTTP 304).
   * Otherwise returns a {@link TldListResponse} whose {@link TldListResponse#getEtag()} can be
   * passed back on a later call to skip unchanged transfers.
   */
  public TldListResponse tlds(TldsOptions options) throws IOException, InterruptedException {
    StringBuilder path = new StringBuilder("/tlds");
    boolean hasQuery = false;
    if (options.getSince() != null) {
      path.append('?').append("since=").append(urlEncode(options.getSince()));
      hasQuery = true;
    }
    if (options.getServer() != null) {
      path.append(hasQuery ? '&' : '?').append("server=").append(urlEncode(options.getServer()));
    }
    HttpResponse<byte[]> response = doConditionalGet(path.toString(), options.getIfNoneMatch());
    if (response.statusCode() == 304) {
      return null;
    }
    TldListResponse result = MAPPER.readValue(response.body(), TldListResponse.class);
    result.setEtag(response.headers().firstValue("ETag").orElse(null));
    return result;
  }

  public TldResponse tld(String tld) throws IOException, InterruptedException {
    return tld(tld, new TldOptions());
  }

  /**
   * Return catalog metadata for a single TLD.
   *
   * <p>Does not count against the monthly quota. Returns {@code null} on HTTP 304. Throws {@link
   * io.rdapapi.client.exceptions.NotFoundException} when no RDAP server is registered for the TLD.
   */
  public TldResponse tld(String tld, TldOptions options) throws IOException, InterruptedException {
    HttpResponse<byte[]> response = doConditionalGet("/tlds/" + tld, options.getIfNoneMatch());
    if (response.statusCode() == 304) {
      return null;
    }
    TldResponse result = MAPPER.readValue(response.body(), TldResponse.class);
    result.setEtag(response.headers().firstValue("ETag").orElse(null));
    return result;
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

  private HttpResponse<byte[]> doConditionalGet(String path, String ifNoneMatch)
      throws IOException, InterruptedException {
    HttpRequest.Builder builder =
        HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + path))
            .header("Authorization", authHeader)
            .header("User-Agent", userAgent)
            .header("Accept", "application/json")
            .GET();
    if (ifNoneMatch != null) {
      builder.header("If-None-Match", ifNoneMatch);
    }
    HttpResponse<byte[]> response =
        httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofByteArray());

    if (response.statusCode() == 304) {
      return response;
    }
    if (response.statusCode() >= 400) {
      handleError(response);
    }
    return response;
  }

  private static String urlEncode(String value) {
    return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
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
        if ("not_supported".equals(errorCode)) {
          return new NotSupportedException(message, errorCode);
        }
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
