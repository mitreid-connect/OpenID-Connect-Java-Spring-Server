package cz.muni.ics.oidc.saml;

import java.util.Collection;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.saml.SAMLCredential;

@Getter
@Setter
@ToString
public class SamlPrincipal extends User {

    private Long perunUserId;
    private SAMLCredential samlCredential;

    public SamlPrincipal(Long perunUserId,
                         SAMLCredential samlCredential,
                         Collection<? extends GrantedAuthority> authorities) {
        super(String.valueOf(perunUserId), "[PROTECTED]", authorities);
        this.perunUserId = perunUserId;
        this.samlCredential = samlCredential;
    }

}
