package cz.muni.ics.oidc.server.claims;

import cz.muni.ics.oidc.models.Facility;

public class ClaimContextCommonParameters {

    private Facility client;

    public ClaimContextCommonParameters(Facility client) {
        this.client = client;
    }

    public Facility getClient() {
        return client;
    }

    public void setClient(Facility client) {
        this.client = client;
    }

}
