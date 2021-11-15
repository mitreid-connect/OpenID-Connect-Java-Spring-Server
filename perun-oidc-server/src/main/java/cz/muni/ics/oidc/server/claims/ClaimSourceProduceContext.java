package cz.muni.ics.oidc.server.claims;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import java.util.Map;

/**
 * Context in which the value of the claim is produced.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
public class ClaimSourceProduceContext {

	private final long perunUserId;
	private final String sub;
	private final Map<String, PerunAttributeValue> attrValues;
	private final PerunAdapter perunAdapter;
	private final ClientDetailsEntity client;
	private final ClaimContextCommonParameters contextCommonParameters;

	public ClaimSourceProduceContext(long perunUserId,
									 String sub,
									 Map<String, PerunAttributeValue> attrValues,
									 PerunAdapter perunAdapter,
									 ClientDetailsEntity client,
									 ClaimContextCommonParameters contextCommonParameters)
	{
		this.perunUserId = perunUserId;
		this.sub = sub;
		this.attrValues = attrValues;
		this.perunAdapter = perunAdapter;
		this.client = client;
		this.contextCommonParameters = contextCommonParameters;
	}

	public Map<String, PerunAttributeValue> getAttrValues() {
		return attrValues;
	}

	public long getPerunUserId() {
		return perunUserId;
	}

	public String getSub() {
		return sub;
	}

	public PerunAdapter getPerunAdapter() {
		return perunAdapter;
	}

	public ClientDetailsEntity getClient() {
		return client;
	}

	public ClaimContextCommonParameters getContextCommonParameters() {
		return contextCommonParameters;
	}

	@Override
	public String toString() {
		return "ClaimSourceProduceContext{" +
				"perunUserId=" + perunUserId +
				", sub='" + sub + '\'' +
				'}';
	}
}
