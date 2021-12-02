package cz.muni.ics.oidc.server.ga4gh;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.web.client.RestTemplate;

@Getter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Ga4ghClaimRepository {

    private final String name;
    private final String actionURL;
    private final RestTemplate restTemplate;

}
