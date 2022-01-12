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
public class PhoneMappings {

    private String phoneNumber = null;
    private String phoneNumberVerified = null;

    public Set<String> getAttrNames() {
        return new HashSet<>(Arrays.asList(phoneNumber, phoneNumberVerified));
    }

}
