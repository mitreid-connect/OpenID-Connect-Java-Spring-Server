package cz.muni.ics.oidc.server.userInfo.mappings;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AddressMappings {

    private String formatted = null;
    private String streetAddress = null;
    private String locality = null;
    private String region = null;
    private String postalCode = null;
    private String country = null;

    public Set<String> getAttrNames() {
        return new HashSet<>(Arrays.asList(formatted, streetAddress, locality, region, postalCode, country));
    }

}
