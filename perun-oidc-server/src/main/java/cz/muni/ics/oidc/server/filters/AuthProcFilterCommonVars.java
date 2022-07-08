package cz.muni.ics.oidc.server.filters;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
public class AuthProcFilterCommonVars {

    private final ClientDetailsEntity client;
    private final Facility facility;
    private final PerunUser user;

    public String getClientIdentifier() {
        if (client != null) {
            return client.getClientId();
        }

        return null;
    }

}
