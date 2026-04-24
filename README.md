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
domain.getDomain();              // "example.com"
domain.getRegistrar().getName(); // Registrar name
domain.getRegistrar().getIanaId(); // IANA registrar ID
domain.isDnssec();               // true/false

// With registrar follow-through (for thin registries)
DomainResponse domain = client.domain("example.com", new DomainOptions().follow(true));
domain.getMeta().getFollowed();  // true
```

### IP Address Lookup

```java
IpResponse ip = client.ip("8.8.8.8");
ip.getName();          // "LVLT-GOGL-8-8-8"
ip.getCountry();       // "US"
ip.getCidr();          // ["8.8.8.0/24"]
ip.getStartAddress();  // "8.8.8.0"
ip.getEndAddress();    // "8.8.8.255"
```

### ASN Lookup

```java
AsnResponse asn = client.asn(15169);        // integer
AsnResponse asn = client.asn("AS15169");    // string with prefix (stripped automatically)

asn.getName();         // "GOOGLE"
asn.getStartAutnum();  // 15169
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

## Supported TLDs Catalog

List every TLD the API can resolve, with the date support was added and a qualitative summary of which fields the registry's RDAP server populates. Does not count against your monthly quota.

```java
import io.rdapapi.client.TldsOptions;
import io.rdapapi.client.responses.TldEntry;
import io.rdapapi.client.responses.TldListResponse;

TldListResponse tlds = client.tlds();
System.out.printf(
    "%d TLDs, coverage %.0f%%%n",
    tlds.getMeta().getCount(), tlds.getMeta().getCoverage() * 100);

for (TldEntry tld : tlds.getData()) {
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
System.out.println(com.getData().getRdapServerHost()); // "rdap.verisign.com"
```

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
} catch (AuthenticationException e) {
    System.out.println("Invalid API key");
} catch (SubscriptionRequiredException e) {
    System.out.println("Subscription required");
}
```

`NotSupportedException` extends `NotFoundException`, so catching `NotFoundException` still handles both cases.

| Exception | HTTP Status | Description |
|---|---|---|
| `ValidationException` | 400 | Invalid input |
| `AuthenticationException` | 401 | Invalid or missing API key |
| `SubscriptionRequiredException` | 403 | No active subscription |
| `NotFoundException` | 404 | Namespace is covered but no record exists |
| `NotSupportedException` | 404 | Namespace (TLD, IP range, ASN range) is not covered by RDAP |
| `RateLimitException` | 429 | Rate limit or quota exceeded |
| `UpstreamException` | 502 | Upstream RDAP server failure |
| `TemporarilyUnavailableException` | 503 | Domain data temporarily unavailable |

All exceptions expose `getStatusCode()`, `getErrorCode()`, and `getMessage()`. `RateLimitException` and `TemporarilyUnavailableException` also have `getRetryAfter()` (Integer or null).

Network errors (`IOException`, `InterruptedException`) are checked exceptions that propagate from `java.net.http.HttpClient`.

## Nullable Fields

Fields that may be absent in API responses return `null`. Check before using:

```java
if (domain.getDates().getExpires() != null) {
    System.out.println("Expires: " + domain.getDates().getExpires());
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
