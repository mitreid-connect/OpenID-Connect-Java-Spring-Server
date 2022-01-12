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
public class EmailMappings {

    private String email = null;
    private String emailVerified = null;

    public Set<String> getAttrNames() {
        return new HashSet<>(Arrays.asList(email, emailVerified));
    }

}
