package io.rdapapi.client.canary;

import static org.assertj.core.api.Assertions.assertThat;

import io.rdapapi.client.DomainOptions;
import io.rdapapi.client.RdapClient;
import io.rdapapi.client.exceptions.RdapApiException;
import io.rdapapi.client.responses.DomainResponse;
import io.rdapapi.client.responses.Entities;
import io.rdapapi.client.responses.IpResponse;
import io.rdapapi.client.responses.Meta;
import io.rdapapi.client.responses.PingResponse;
import io.rdapapi.client.responses.TldEntry;
import io.rdapapi.client.responses.TldResponse;
import java.io.IOException;
import java.time.Duration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Calls production through the SDK's own public API and fails when the live contract stops matching
 * what the SDK models.
 *
 * <p>Deliberately outside {@code src/test}: {@code check} runs offline behind a coverage gate, and
 * a network probe there would break it. Run with {@code ./gradlew canary} and {@code
 * RDAPAPI_API_KEY} set.
 */
class ProductionCanaryTest {

  private static final Duration RETRY_BACKOFF = Duration.ofSeconds(5);

  private static RdapClient client;

  @BeforeAll
  static void openClient() {
    String apiKey = System.getenv("RDAPAPI_API_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      throw new IllegalStateException("RDAPAPI_API_KEY is not set; the canary needs a live key");
    }
    client = new RdapClient(apiKey);
  }

  @AfterAll
  static void closeClient() {
    if (client != null) {
      client.close();
    }
  }

  @Test
  void pingAnswersOk() throws Exception {
    PingResponse ping = retryingOnce(client::ping);

    assertThat(ping.getStatus()).as("ping status, expected \"ok\"").isEqualTo("ok");
  }

  @Test
  void rdapDomainCarriesRegistrarAndEntities() throws Exception {
    DomainResponse domain =
        retryingOnce(() -> client.domain("google.com", new DomainOptions().follow(true)));

    Meta meta = domain.getMeta();
    assertThat(meta).as("google.com meta, expected present").isNotNull();
    assertThat(meta.getSource()).as("google.com meta.source, expected \"rdap\"").isEqualTo("rdap");
    assertThat(meta.getServer()).as("google.com meta.server, expected non-empty").isNotEmpty();
    assertThat(domain.getRegistrar()).as("google.com registrar, expected present").isNotNull();
    assertThat(domain.getRegistrar().getName())
        .as("google.com registrar.name, expected non-empty")
        .isNotEmpty();
    assertThat(contactCount(domain.getEntities()))
        .as("google.com entity count with follow=true, expected at least one")
        .isPositive();
  }

  @Test
  @SuppressWarnings("deprecation")
  void whoisDomainLeavesRdapFieldsNull() throws Exception {
    DomainResponse domain = retryingOnce(() -> client.domain("google.it"));

    Meta meta = domain.getMeta();
    assertThat(meta).as("google.it meta, expected present").isNotNull();
    assertThat(meta.getSource()).as("google.it meta.source, expected \"whois\"").isEqualTo("whois");
    assertThat(meta.getServer())
        .as("google.it meta.server, expected \"whois.nic.it\"")
        .isEqualTo("whois.nic.it");
    // A WHOIS answer has no RDAP endpoint: reading these must give null, not throw. Returning a
    // shape the SDK could not read is what crashed the Python SDK in production.
    assertThat(meta.getRdapServer()).as("google.it meta.rdap_server, expected null").isNull();
    assertThat(meta.getRawRdapUrl()).as("google.it meta.raw_rdap_url, expected null").isNull();
  }

  @Test
  void ipPublishesGeofeed() throws Exception {
    IpResponse ip = retryingOnce(() -> client.ip("45.83.220.1"));

    // The geofeed is published by the network holder, not by us. If this ever fails, check that
    // they still publish one before suspecting the SDK.
    assertThat(ip.getGeofeed()).as("45.83.220.1 geofeed, expected non-empty").isNotEmpty();
  }

  @Test
  @SuppressWarnings("deprecation")
  void whoisTldLeavesRdapFieldsNull() throws Exception {
    TldResponse response = retryingOnce(() -> client.tld("it"));

    assertThat(response).as("tld \"it\" response, expected present (not a 304)").isNotNull();
    TldEntry entry = response.getData();
    assertThat(entry).as("tld \"it\" data, expected present").isNotNull();
    assertThat(entry.getProtocol())
        .as("tld \"it\" protocol, expected \"whois\"")
        .isEqualTo("whois");
    assertThat(entry.getServer()).as("tld \"it\" server, expected non-empty").isNotEmpty();
    assertThat(entry.getRdapServerHost()).as("tld \"it\" rdap_server_host, expected null").isNull();
    assertThat(entry.getRdapServerUrl()).as("tld \"it\" rdap_server_url, expected null").isNull();
  }

  private static int contactCount(Entities entities) {
    if (entities == null) {
      return 0;
    }
    int count = 0;
    for (Object contact :
        new Object[] {
          entities.getRegistrant(),
          entities.getAdministrative(),
          entities.getTechnical(),
          entities.getBilling(),
          entities.getAbuse()
        }) {
      if (contact != null) {
        count++;
      }
    }
    return count;
  }

  /**
   * Runs a probe, retrying once after a transport error or a 5xx. A contract assertion is never
   * retried — a field that is wrong is wrong.
   */
  private static <T> T retryingOnce(Probe<T> probe) throws Exception {
    try {
      return probe.call();
    } catch (IOException e) {
      return retryAfterBackoff(probe, e.toString());
    } catch (RdapApiException e) {
      if (e.getStatusCode() < 500) {
        throw e;
      }
      return retryAfterBackoff(probe, "HTTP " + e.getStatusCode() + ": " + e.getMessage());
    }
  }

  private static <T> T retryAfterBackoff(Probe<T> probe, String reason) throws Exception {
    System.out.println("Transient failure (" + reason + "), retrying once in " + RETRY_BACKOFF);
    Thread.sleep(RETRY_BACKOFF.toMillis());
    return probe.call();
  }

  @FunctionalInterface
  private interface Probe<T> {
    T call() throws Exception;
  }
}
