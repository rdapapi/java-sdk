import io.rdapapi.client.RdapClient;
import io.rdapapi.client.TldOptions;
import io.rdapapi.client.TldsOptions;
import io.rdapapi.client.responses.FieldAvailability;
import io.rdapapi.client.responses.TldEntry;
import io.rdapapi.client.responses.TldListResponse;
import io.rdapapi.client.responses.TldResponse;

public class TldsCatalog {

  public static void main(String[] args) throws Exception {
    String apiKey = System.getenv("RDAPAPI_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Set RDAPAPI_KEY environment variable");
      System.exit(1);
    }

    try (RdapClient client = new RdapClient(apiKey)) {
      // Full catalog. Does not count against your monthly quota.
      TldListResponse tlds = client.tlds();
      System.out.printf(
          "%d TLDs supported, coverage %.0f%%%n",
          tlds.getMeta().getCount(), tlds.getMeta().getCoverage() * 100);

      int shown = 0;
      for (TldEntry tld : tlds.getData()) {
        if (shown++ >= 5) break;
        FieldAvailability availability = tld.getFieldAvailability();
        if (availability == null) {
          System.out.printf(".%s via %s (not enough data yet)%n", tld.getTld(), tld.getRdapServerHost());
        } else {
          System.out.printf(
              ".%s via %s: registrar=%s, expires_at=%s%n",
              tld.getTld(),
              tld.getRdapServerHost(),
              availability.getRegistrar().toWire(),
              availability.getExpiresAt().toWire());
        }
      }

      // Skip the transfer when nothing has changed.
      TldListResponse later = client.tlds(new TldsOptions().ifNoneMatch(tlds.getEtag()));
      System.out.println(later == null ? "No change since last poll" : "Changed");

      // Single-TLD lookup.
      TldResponse com = client.tld("com", new TldOptions());
      if (com != null) {
        System.out.println(".com supported since " + com.getData().getSupportedSince());
      }
    }
  }
}
