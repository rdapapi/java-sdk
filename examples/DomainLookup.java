import io.rdapapi.client.DomainOptions;
import io.rdapapi.client.RdapClient;
import io.rdapapi.client.responses.DomainResponse;

public class DomainLookup {

  public static void main(String[] args) throws Exception {
    String apiKey = System.getenv("RDAPAPI_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Set RDAPAPI_KEY environment variable");
      System.exit(1);
    }

    try (RdapClient client = new RdapClient(apiKey)) {
      // Basic domain lookup.
      DomainResponse domain = client.domain("google.com");

      System.out.println("Domain: " + domain.getDomain());
      System.out.println("Registrar: " + domain.getRegistrar().getName());
      System.out.println("Registered: " + domain.getDates().getRegistered());
      System.out.println("Expires: " + domain.getDates().getExpires());
      System.out.println("Status: " + String.join(", ", domain.getStatus()));
      System.out.println("Nameservers: " + String.join(", ", domain.getNameservers()));
      System.out.println("DNSSEC: " + (domain.isDnssec() ? "yes" : "no"));

      // With registrar follow-through.
      DomainResponse followed = client.domain("google.com", new DomainOptions().follow(true));

      System.out.println("\n--- With follow ---");
      System.out.println("Followed: " + followed.getMeta().getFollowed());
      if (followed.getEntities().getRegistrant() != null) {
        System.out.println("Registrant: " + followed.getEntities().getRegistrant().getName());
      }
    }
  }
}
