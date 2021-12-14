package cz.muni.ics.oauth2.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ClientWithScopes {

    private ClientDetailsEntity client;
    private Set<String> requestedScopes;

}
