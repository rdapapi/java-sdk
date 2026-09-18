package io.rdapapi.client;

import static org.assertj.core.api.Assertions.*;

import io.rdapapi.client.exceptions.*;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ExceptionsTest {

  @Test
  void baseExceptionProperties() {
    RdapApiException ex = new RdapApiException("test message", 500, "server_error");
    assertThat(ex.getMessage()).isEqualTo("test message");
    assertThat(ex.getStatusCode()).isEqualTo(500);
    assertThat(ex.getErrorCode()).isEqualTo("server_error");
    assertThat(ex).isInstanceOf(RuntimeException.class);
  }

  @Test
  void validationException() {
    ValidationException ex = new ValidationException("bad input", "invalid_domain");
    assertThat(ex.getStatusCode()).isEqualTo(400);
    assertThat(ex.getErrorCode()).isEqualTo("invalid_domain");
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void authenticationException() {
    AuthenticationException ex = new AuthenticationException("bad key", "unauthenticated");
    assertThat(ex.getStatusCode()).isEqualTo(401);
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void subscriptionRequiredException() {
    SubscriptionRequiredException ex =
        new SubscriptionRequiredException("no sub", "subscription_required");
    assertThat(ex.getStatusCode()).isEqualTo(403);
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void notFoundException() {
    NotFoundException ex = new NotFoundException("not found", "not_found");
    assertThat(ex.getStatusCode()).isEqualTo(404);
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void notSupportedExceptionExtendsNotFound() {
    NotSupportedException ex = new NotSupportedException("TLD not supported", "not_supported");
    assertThat(ex.getStatusCode()).isEqualTo(404);
    assertThat(ex.getErrorCode()).isEqualTo("not_supported");
    assertThat(ex).isInstanceOf(NotFoundException.class);
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void rateLimitExceptionWithRetryAfter() {
    RateLimitException ex = new RateLimitException("rate limited", "rate_limit_exceeded", 60);
    assertThat(ex.getStatusCode()).isEqualTo(429);
    assertThat(ex.getRetryAfter()).isEqualTo(60);
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void rateLimitExceptionWithoutRetryAfter() {
    RateLimitException ex = new RateLimitException("rate limited", "rate_limit_exceeded", null);
    assertThat(ex.getRetryAfter()).isNull();
  }

  @Test
  void temporarilyUnavailableExceptionWithRetryAfter() {
    TemporarilyUnavailableException ex =
        new TemporarilyUnavailableException("unavailable", "temporarily_unavailable", 300);
    assertThat(ex.getStatusCode()).isEqualTo(503);
    assertThat(ex.getRetryAfter()).isEqualTo(300);
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void temporarilyUnavailableExceptionWithoutRetryAfter() {
    TemporarilyUnavailableException ex =
        new TemporarilyUnavailableException("unavailable", "temporarily_unavailable", null);
    assertThat(ex.getRetryAfter()).isNull();
  }

  @Test
  void upstreamException() {
    UpstreamException ex = new UpstreamException("upstream fail", "lookup_failed");
    assertThat(ex.getStatusCode()).isEqualTo(502);
    assertThat(ex.getRetryAfter()).isNull();
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void upstreamExceptionWithRetryAfter() {
    UpstreamException ex = new UpstreamException("upstream fail", "lookup_failed", 60);
    assertThat(ex.getRetryAfter()).isEqualTo(60);
  }

  @Test
  void requestFailedException() {
    RequestFailedException ex =
        new RequestFailedException(
            "The domains field is required.",
            "request_failed",
            Map.of("domains", List.of("The domains field is required.")));
    assertThat(ex.getStatusCode()).isEqualTo(422);
    assertThat(ex.getErrorCode()).isEqualTo("request_failed");
    assertThat(ex.getErrors()).containsOnlyKeys("domains");
    assertThat(ex).isInstanceOf(RdapApiException.class);
  }

  @Test
  void requestFailedExceptionWithoutFieldErrors() {
    RequestFailedException ex =
        new RequestFailedException("The body failed validation.", "request_failed", null);
    assertThat(ex.getErrors()).isEmpty();
  }

  @Test
  void exceptionHierarchy() {
    try {
      throw new NotFoundException("test", "not_found");
    } catch (RdapApiException ex) {
      assertThat(ex.getStatusCode()).isEqualTo(404);
    }
  }
}
