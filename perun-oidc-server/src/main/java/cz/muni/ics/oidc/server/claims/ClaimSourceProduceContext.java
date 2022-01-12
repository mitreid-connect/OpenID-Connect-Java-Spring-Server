package cz.muni.ics.oidc.server.claims;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Context in which the value of the claim is produced.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaimSourceProduceContext {

	private long perunUserId;
	private String sub;
	private Map<String, PerunAttributeValue> attrValues;
	private PerunAdapter perunAdapter;
	private ClientDetailsEntity client;
	private Facility facility;
	private SamlAuthenticationDetails samlAuthenticationDetails;
	private Set<String> scopes;

}
