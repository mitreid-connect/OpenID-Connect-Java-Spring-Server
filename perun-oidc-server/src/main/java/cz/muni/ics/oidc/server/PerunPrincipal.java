package cz.muni.ics.oidc.server;

/**
 * Principal specific for Perun user. User is identified by login (extLogin) and name
 * of the external source (extSourceName) he/she used for login (usually identity provider).
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
public class PerunPrincipal {

	private final String extLogin;
	private final String extSourceName;

	public PerunPrincipal(String extLogin, String extSourceName) {
		this.extLogin = extLogin;
		this.extSourceName = extSourceName;
	}

	public String getExtLogin() {
		return extLogin;
	}

	public String getExtSourceName() {
		return extSourceName;
	}

	@Override
	public String toString() {
		return "PerunPrincipal{" +
				"extLogin='" + extLogin + '\'' +
				", extSourceName='" + extSourceName + '\'' +
				'}';
	}

}
