# rdapapi-java

Official Java SDK for the [RDAP API](https://rdapapi.io) — look up domains, IP addresses, ASNs, nameservers, and entities via the RDAP protocol.

[![Maven Central](https://img.shields.io/maven-central/v/io.rdapapi/rdapapi-java.svg)](https://central.sonatype.com/artifact/io.rdapapi/rdapapi-java)
[![CI](https://github.com/rdapapi/java-sdk/actions/workflows/ci.yml/badge.svg)](https://github.com/rdapapi/java-sdk/actions/workflows/ci.yml)

## Installation

### Gradle

```kotlin
implementation("io.rdapapi:rdapapi-java:0.5.0")
```

### Maven

```xml
<dependency>
    <groupId>io.rdapapi</groupId>
    <artifactId>rdapapi-java</artifactId>
    <version>0.5.0</version>
</dependency>
```

Requires Java 11 or later.

## Quick Start

```java
import io.rdapapi.client.RdapClient;
import io.rdapapi.client.responses.DomainResponse;

try (RdapClient client = new RdapClient("your-api-key")) {
    DomainResponse domain = client.domain("google.com");

    System.out.println(domain.getRegistrar().getName());   // "MarkMonitor Inc."
    System.out.println(domain.getDates().getRegistered()); // "1997-09-15T04:00:00Z"
    System.out.println(domain.getDates().getExpires());    // "2028-09-14T04:00:00Z"
    System.out.println(domain.getNameservers());           // ["ns1.google.com", ...]
}
```

## Usage

### Configuration

```java
import io.rdapapi.client.RdapClient;
import io.rdapapi.client.RdapClientOptions;
import java.time.Duration;

// Default configuration
RdapClient client = new RdapClient("your-api-key");

// Custom timeout
RdapClient client = new RdapClient("your-api-key",
    new RdapClientOptions().timeout(Duration.ofSeconds(10)));

// Custom base URL
RdapClient client = new RdapClient("your-api-key",
    new RdapClientOptions().baseUrl("https://custom.api.com/v1"));
```

The client implements `AutoCloseable` and can be used with try-with-resources.

### Domain Lookup

```java
DomainResponse domain = client.domain("example.com");
domain.getDomain();                // "example.com"
domain.getRegistrar().getName();   // Registrar name
domain.getRegistrar().getIanaId(); // IANA registrar ID
domain.getDnssec();                // TRUE, FALSE, or null where the registry publishes no status
domain.getMeta().getServer();      // "rdap.verisign.com" — the upstream that answered
domain.getMeta().getSource();      // "rdap" or "whois"

// With registrar follow-through (for thin registries)
DomainResponse domain = client.domain("example.com", new DomainOptions().follow(true));
domain.getMeta().getFollowed();  // true
```

A TLD with no RDAP server is answered over the registry's WHOIS server and comes back in the same
shape, with `getMeta().getSource()` returning `"whois"`. Refuse that fallback to get a
`NotSupportedException` instead:

```java
DomainResponse domain = client.domain("example.it", new DomainOptions().whois(false));
```

### Redacted Fields

Since GDPR most contact fields come back `null`, and a field the registry never collected looks
exactly like one it withheld. `getRedacted()` reports what the upstream server *declared* it
withheld, mirroring the shape of the record. It is `null` when the server declared nothing — which
is not evidence that nothing was withheld.

The maps are never null, but a role or field the server said nothing about is simply absent — a
response whose only claim is on `handle` is the common case — so reach for the inner map with
`getOrDefault`, never by chaining `get()`:

```java
import io.rdapapi.client.responses.Redaction;
import io.rdapapi.client.responses.RedactionMethod;
import java.util.Map;

Redaction redacted = domain.getRedacted();
if (redacted != null) {
    redacted.getHandle();                   // "replacementValue", or null
    redacted.getRegistrar().get("iana_id"); // method used on registrar.iana_id, or null

    // getEntities().get("registrant") is null unless the server declared a claim on that role.
    redacted.getEntities()
        .getOrDefault("registrant", Map.of())
        .get("email");                      // method used on that contact field, or null
}
```

Methods are plain strings — `RedactionMethod.REMOVAL`, `EMPTY_VALUE`, `PARTIAL_VALUE`,
`REPLACEMENT_VALUE` — and a method we do not recognise is passed through unchanged, so compare
rather than assume.

### IP Address Lookup

```java
IpResponse ip = client.ip("8.8.8.8");
ip.getName();          // "LVLT-GOGL-8-8-8"
ip.getCountry();       // "US"
ip.getCidr();          // ["8.8.8.0/24"]
ip.getStartAddress();  // "8.8.8.0"
ip.getEndAddress();    // "8.8.8.255"
ip.getGeofeed();       // RFC 8805 geofeed URL the network publishes, or null
```

Pass a CIDR block to get that network rather than the most specific allocation covering an address:

```java
IpResponse block = client.ip("8.8.8.0/24");
```

### ASN Lookup

```java
AsnResponse asn = client.asn(15169);        // integer
AsnResponse asn = client.asn("AS15169");    // string with prefix (stripped automatically)

asn.getName();         // "GOOGLE"
asn.getStartAutnum();  // 15169
asn.getCountry();      // "US", from the contact entities' address, or null
```

### Nameserver Lookup

```java
NameserverResponse ns = client.nameserver("ns1.google.com");
ns.getLdhName();                // "ns1.google.com"
ns.getIpAddresses().getV4();   // ["216.239.32.10"]
ns.getIpAddresses().getV6();   // ["2001:4860:4802:32::a"]
```

### Entity Lookup

```java
EntityResponse entity = client.entity("GOGL");
entity.getName();                        // "Google LLC"
entity.getAutnums().get(0).getHandle();  // "AS15169"
entity.getNetworks().get(0).getCidr();   // ["8.8.8.0/24"]
```

### Bulk Domain Lookup

Requires a Pro or Business plan. Up to 10 domains per call.

```java
BulkDomainResponse resp = client.bulkDomains(
    List.of("google.com", "github.com", "example.com"),
    new DomainOptions().follow(true));

resp.getSummary().getTotal();      // 3
resp.getSummary().getSuccessful(); // 3

for (BulkDomainResult result : resp.getResults()) {
    if ("success".equals(result.getStatus())) {
        System.out.println(result.getDomain() + " — " + result.getData().getRegistrar().getName());
    } else {
        System.out.println(result.getDomain() + " — error: " + result.getMessage());
    }
}
```

`follow` and `whois` apply to every domain in the request. A failed entry's `getMeta()` names the
upstream that was tried, and is null when the entry failed before one was chosen. That partial meta
carries no cache state, so `getCached()` and `getCacheExpires()` are null there rather than false.

## Health Check

```java
client.ping().getStatus(); // "ok"
```

Sent with your API key like every other call, but makes no upstream RDAP call and never counts
against your quota.

## Supported TLDs Catalog

List every TLD the API can resolve, with the protocol and server that answers for it, the date support was added, and a qualitative summary of which fields the registry's RDAP server populates. Does not count against your monthly quota.

```java
import io.rdapapi.client.TldsOptions;
import io.rdapapi.client.responses.TldEntry;
import io.rdapapi.client.responses.TldListResponse;

TldListResponse tlds = client.tlds();
System.out.printf(
    "%d TLDs, coverage %.0f%%%n",
    tlds.getMeta().getCount(), tlds.getMeta().getCoverage() * 100);

for (TldEntry tld : tlds.getData()) {
    tld.getProtocol();  // "rdap", or "whois" for the ccTLDs IANA lists no RDAP server for
    tld.getServer();    // hostname of the upstream that answers, as meta.server returns it

    if (tld.getFieldAvailability() != null) {
        System.out.printf(
            "%s: expires_at=%s%n",
            tld.getTld(), tld.getFieldAvailability().getExpiresAt().toWire());
    }
}
```

Filter to recent additions or to a single registry:

```java
TldListResponse recent = client.tlds(new TldsOptions().since("2026-04-01T00:00:00Z"));
TldListResponse verisign = client.tlds(new TldsOptions().server("rdap.verisign.com"));
```

Pass back the previous ETag to skip the transfer when nothing has changed. The method returns `null` on HTTP 304:

```java
TldListResponse first = client.tlds();
TldListResponse later = client.tlds(new TldsOptions().ifNoneMatch(first.getEtag()));
if (later == null) {
    System.out.println("No change since last poll");
}
```

Look up a single TLD:

```java
TldResponse com = client.tld("com");
System.out.println(com.getData().getServer()); // "rdap.verisign.com"
```

`getRdapServerHost()` is deprecated in favour of `getServer()`, as is `Meta.getRdapServer()` in
favour of `Meta.getServer()`.

## Error Handling

All API errors are thrown as unchecked exceptions that extend `RdapApiException`:

```java
import io.rdapapi.client.exceptions.*;

try {
    client.domain("example.nope");
    // Catch NotSupportedException before NotFoundException: it's a subclass.
} catch (NotSupportedException e) {
    System.out.println("TLD not covered by RDAP: " + e.getMessage());
} catch (NotFoundException e) {
    System.out.println("Domain not registered: " + e.getMessage());
} catch (RateLimitException e) {
    System.out.println("Rate limited, retry after " + e.getRetryAfter() + " seconds");
} catch (RequestFailedException e) {
    System.out.println("Invalid request: " + e.getErrors());
} catch (AuthenticationException e) {
    System.out.println("Invalid API key");
} catch (SubscriptionRequiredException e) {
    // One class, three situations — branch on the code, not the message.
    if ("forbidden".equals(e.getErrorCode())) {
        System.out.println("This IP is blocked; subscribing will not lift it");
    } else if ("plan_upgrade_required".equals(e.getErrorCode())) {
        System.out.println("This endpoint needs a higher plan");
    } else {
        System.out.println("Subscription required");
    }
}
```

`NotSupportedException` extends `NotFoundException`, so catching `NotFoundException` still handles both cases.

| Exception | HTTP Status | Description |
|---|---|---|
| `ValidationException` | 400 | Invalid input |
| `AuthenticationException` | 401 | Invalid or missing API key |
| `SubscriptionRequiredException` | 403 | Refused for the account: `subscription_required`, `plan_upgrade_required`, or `forbidden` (a blocked IP, which no subscription changes) |
| `NotFoundException` | 404 | Namespace is covered but no record exists |
| `NotSupportedException` | 404 | Namespace (TLD, IP range, ASN range) is not covered by RDAP |
| `RequestFailedException` | 422 | Request body failed validation; `getErrors()` names the fields |
| `RateLimitException` | 429 | Rate limit or quota exceeded |
| `UpstreamException` | 502 | Upstream RDAP server failure |
| `TemporarilyUnavailableException` | 503 | Domain data temporarily unavailable |

One exception class can cover several error codes, so where the remedy differs — 403 above is the
case that bites — branch on `getErrorCode()` inside the catch. Any other status — `405`, `413`,
`504`, `5xx` — arrives as the base `RdapApiException`. Branch on `getErrorCode()`, never on the
message: `invalid_domain`, `invalid_ip`, `invalid_asn`, `invalid_nameserver`, `invalid_handle`,
`invalid_prefix`, `invalid_since`, `bad_request`, `unauthenticated`, `subscription_required`,
`plan_upgrade_required`, `forbidden`, `not_found`, `not_supported`, `method_not_allowed`,
`payload_too_large`, `request_failed`, `rate_limit_exceeded`, `quota_exceeded`,
`too_many_requests`, `lookup_failed`, `bad_gateway`, `temporarily_unavailable`,
`service_unavailable`, `gateway_timeout`, `server_error`.

All exceptions expose `getStatusCode()`, `getErrorCode()`, and `getMessage()`. `RateLimitException`, `UpstreamException` and `TemporarilyUnavailableException` also have `getRetryAfter()` (Integer or null), in seconds. It is read from the `Retry-After` header when there is one — in either RFC 9110 form, delta-seconds or an HTTP-date, since a registry's header is passed through verbatim — and from the body's `retry_after` otherwise.

Network errors (`IOException`, `InterruptedException`) are checked exceptions that propagate from `java.net.http.HttpClient`.

## Nullable Fields

Fields that may be absent in API responses return `null`. Check before using:

```java
if (domain.getDates().getExpires() != null) {
    System.out.println("Expires: " + domain.getDates().getExpires());
}

// getDnssec() is a Boolean: null means the registry publishes no DNSSEC status,
// as .tr, .gg and .nc do not. Never unbox it without a null check.
if (Boolean.TRUE.equals(domain.getDnssec())) {
    System.out.println("Signed delegation");
}

// Contact fields may be null
if (domain.getEntities().getRegistrant() != null) {
    System.out.println("Registrant: " + domain.getEntities().getRegistrant().getName());
}
```

List fields (`getStatus()`, `getNameservers()`, `getCidr()`, etc.) never return null — they return an empty unmodifiable list when absent.

## Development

Set up pre-commit hooks (runs lint + tests before each commit):

```bash
git config core.hooksPath .githooks
```

## License

MIT — see [LICENSE](LICENSE).
