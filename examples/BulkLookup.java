import io.rdapapi.client.DomainOptions;
import io.rdapapi.client.RdapClient;
import io.rdapapi.client.responses.BulkDomainResponse;
import io.rdapapi.client.responses.BulkDomainResult;
import java.util.Arrays;

public class BulkLookup {

  public static void main(String[] args) throws Exception {
    String apiKey = System.getenv("RDAPAPI_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Set RDAPAPI_KEY environment variable");
      System.exit(1);
    }

    try (RdapClient client = new RdapClient(apiKey)) {
      BulkDomainResponse resp =
          client.bulkDomains(
              Arrays.asList("google.com", "github.com", "example.com"),
              new DomainOptions().follow(true));

      System.out.println("Total: " + resp.getSummary().getTotal());
      System.out.println("Successful: " + resp.getSummary().getSuccessful());
      System.out.println("Failed: " + resp.getSummary().getFailed());
      System.out.println();

      for (BulkDomainResult result : resp.getResults()) {
        if ("success".equals(result.getStatus())) {
          System.out.println(result.getDomain() + " — " + result.getData().getRegistrar().getName());
        } else {
          System.out.println(result.getDomain() + " — error: " + result.getMessage());
        }
      }
    }
  }
}
