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
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class RdapClient implements AutoCloseable {

  private static final String DEFAULT_BASE_URL = "https://rdapapi.io/api/v1";
  private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
  private static final Pattern DELTA_SECONDS = Pattern.compile("\\d+");

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
    StringBuilder path = new StringBuilder("/domain/").append(name);
    boolean hasQuery = false;
    if (options.isFollow()) {
      path.append("?follow=true");
      hasQuery = true;
    }
    if (!options.isWhois()) {
      path.append(hasQuery ? '&' : '?').append("whois=false");
    }
    byte[] body = doGet(path.toString());
    return MAPPER.readValue(body, DomainResponse.class);
  }

  /**
   * Liveness probe for uptime monitoring.
   *
   * <p>Sent with your API key like every other call, but makes no upstream RDAP call and never
   * counts against your quota.
   */
  public PingResponse ping() throws IOException, InterruptedException {
    byte[] body = doGet("/ping");
    return MAPPER.readValue(body, PingResponse.class);
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
   * List every TLD the API can resolve, over RDAP or the WHOIS fallback.
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
    if (!options.isWhois()) {
      root.put("whois", false);
    }

    byte[] responseBody = doPost("/domains/bulk", MAPPER.writeValueAsBytes(root));

    JsonNode tree = MAPPER.readTree(responseBody);
    JsonNode results = tree.get("results");
    if (results != null && results.isArray()) {
      for (JsonNode result : results) {
        // A successful entry carries its meta beside the data rather than inside it; copy it in
        // so DomainResponse.getMeta() works the same as on a single lookup.
        if ("success".equals(result.path("status").asText())
            && result.has("data")
            && result.has("meta")) {
          ((ObjectNode) result.get("data")).set("meta", result.get("meta"));
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
    JsonNode body = null;
    try {
      body = MAPPER.readTree(response.body());
      if (body.has("error")) {
        errorCode = body.get("error").asText();
      }
      if (body.has("message")) {
        message = body.get("message").asText();
      }
    } catch (Exception ignored) {
      // non-JSON error body — use defaults
    }

    throw createException(
        response.statusCode(),
        errorCode,
        message,
        retryAfter(response, body),
        validationErrors(response.statusCode(), body));
  }

  /** Seconds to wait, from the header when there is one and the body otherwise. */
  private static Integer retryAfter(HttpResponse<byte[]> response, JsonNode body) {
    int statusCode = response.statusCode();
    if (statusCode != 429 && statusCode != 502 && statusCode != 503) {
      return null;
    }
    Integer fromHeader = parseRetryAfter(response.headers().firstValue("Retry-After").orElse(null));
    if (fromHeader != null) {
      return fromHeader;
    }
    if (body != null && body.path("retry_after").isInt()) {
      return body.get("retry_after").asInt();
    }
    return null;
  }

  /**
   * A {@code Retry-After} header in either RFC 9110 form.
   *
   * <p>The API passes an upstream registry's header through verbatim, so the HTTP-date form really
   * arrives. A date already past reads as 0, as does a literal {@code 0}; null means the header
   * yielded nothing, so the caller falls back to the body rather than reading a zero as absent.
   */
  private static Integer parseRetryAfter(String header) {
    if (header == null) {
      return null;
    }
    String value = header.trim();
    if (DELTA_SECONDS.matcher(value).matches()) {
      // Clamp rather than overflow: nothing caps the seconds a header may name.
      return value.length() > 9 ? Integer.MAX_VALUE : Integer.parseInt(value);
    }
    try {
      Instant at = ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
      long seconds = Duration.between(Instant.now(), at).getSeconds();
      return (int) Math.min(Integer.MAX_VALUE, Math.max(0, seconds));
    } catch (DateTimeParseException ignored) {
      return null;
    }
  }

  /** Per-field messages a 422 names, keyed by field path. */
  private static Map<String, List<String>> validationErrors(int statusCode, JsonNode body) {
    if (statusCode != 422 || body == null || !body.path("errors").isObject()) {
      return null;
    }
    Map<String, List<String>> errors = new LinkedHashMap<>();
    body.get("errors")
        .fields()
        .forEachRemaining(
            field -> {
              List<String> messages = new ArrayList<>();
              JsonNode value = field.getValue();
              // A field usually maps to an array of messages, but a bare string arrives too,
              // and iterating a value node would silently drop the reason.
              if (value.isArray()) {
                value.forEach(node -> messages.add(node.asText()));
              } else {
                messages.add(value.asText());
              }
              errors.put(field.getKey(), messages);
            });
    return errors;
  }

  private RdapApiException createException(
      int statusCode,
      String errorCode,
      String message,
      Integer retryAfter,
      Map<String, List<String>> errors) {
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
      case 422:
        return new RequestFailedException(message, errorCode, errors);
      case 429:
        return new RateLimitException(message, errorCode, retryAfter);
      case 502:
        return new UpstreamException(message, errorCode, retryAfter);
      case 503:
        return new TemporarilyUnavailableException(message, errorCode, retryAfter);
      default:
        return new RdapApiException(message, statusCode, errorCode);
    }
  }
}
