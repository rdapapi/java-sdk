import io.rdapapi.client.RdapClient;
import io.rdapapi.client.responses.AsnResponse;
import io.rdapapi.client.responses.IpResponse;
import io.rdapapi.client.responses.NameserverResponse;

public class IpLookup {

  public static void main(String[] args) throws Exception {
    String apiKey = System.getenv("RDAPAPI_KEY");
    if (apiKey == null || apiKey.isEmpty()) {
      System.err.println("Set RDAPAPI_KEY environment variable");
      System.exit(1);
    }

    try (RdapClient client = new RdapClient(apiKey)) {
      // IP lookup.
      IpResponse ip = client.ip("8.8.8.8");
      System.out.println("IP Name: " + ip.getName());
      System.out.println("Country: " + ip.getCountry());
      System.out.println("CIDR: " + String.join(", ", ip.getCidr()));
      System.out.println("Range: " + ip.getStartAddress() + " - " + ip.getEndAddress());

      // ASN lookup.
      AsnResponse asn = client.asn(15169);
      System.out.println("\nASN Name: " + asn.getName());
      System.out.println("ASN Handle: " + asn.getHandle());

      // Nameserver lookup.
      NameserverResponse ns = client.nameserver("ns1.google.com");
      System.out.println("\nNameserver: " + ns.getLdhName());
      System.out.println("IPv4: " + String.join(", ", ns.getIpAddresses().getV4()));
      System.out.println("IPv6: " + String.join(", ", ns.getIpAddresses().getV6()));
    }
  }
}
