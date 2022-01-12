package cz.muni.ics.oidc.server.userInfo;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oauth2.model.SamlAuthenticationDetails;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserInfoCacheKey {

	private final long userId;
	private final ClientDetailsEntity client;
	private final SamlAuthenticationDetails authenticationDetails;
	private final Set<String> scopes;

	public UserInfoCacheKey(String userId,
							ClientDetailsEntity client,
							SamlAuthenticationDetails authenticationDetails,
							Set<String> scopes)
	{
		this.userId = Long.parseLong(userId);
		this.client = client;
		this.authenticationDetails = authenticationDetails;
		this.scopes = scopes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UserInfoCacheKey that = (UserInfoCacheKey) o;
		String ourClientId = client != null ? client.getClientId() : null;
		String theirClientId = that.client != null ? that.client.getClientId() : null;
		return userId == that.userId
				&& Objects.equals(ourClientId, theirClientId)
				&& Objects.equals(authenticationDetails, that.authenticationDetails)
				&& Objects.equals(scopes, that.scopes);
	}

	@Override
	public int hashCode() {
		String clientId = client != null ? client.getClientId() : null;
		if (clientId != null) {
			return Objects.hash(userId, clientId, authenticationDetails, scopes);
		} else {
			return Objects.hash(userId, authenticationDetails, scopes);
		}
	}

}
