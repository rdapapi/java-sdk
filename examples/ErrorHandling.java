import io.rdapapi.client.RdapClient;
import io.rdapapi.client.exceptions.*;

public class ErrorHandling {

  public static void main(String[] args) throws Exception {
    String apiKey = System.getenv("RDAPAPI_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Set RDAPAPI_KEY environment variable");
      System.exit(1);
    }

    try (RdapClient client = new RdapClient(apiKey)) {
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
      } catch (RdapApiException e) {
        System.out.println("API error " + e.getStatusCode() + ": " + e.getMessage());
      }
    }
  }
}
