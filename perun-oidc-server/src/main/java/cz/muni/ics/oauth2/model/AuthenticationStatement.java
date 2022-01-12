package cz.muni.ics.oauth2.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class AuthenticationStatement {

    private List<String> authenticatingAuthorities;
    private String authnContextClassRef;

}
