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
public class ProfileMappings {

    private String name = null;
    private String familyName = null;
    private String givenName = null;
    private String middleName = null;
    private String nickname = null;
    private String preferredUsername = null;
    private String profile = null;
    private String picture = null;
    private String website = null;
    private String gender = null;
    private String birthdate = null;
    private String zoneinfo = null;
    private String locale = null;

    public Set<String> getAttrNames() {
        return new HashSet<>(Arrays.asList(name, familyName, givenName, middleName, nickname, preferredUsername,
                profile, picture, website, gender, birthdate, zoneinfo, locale));
    }

}
