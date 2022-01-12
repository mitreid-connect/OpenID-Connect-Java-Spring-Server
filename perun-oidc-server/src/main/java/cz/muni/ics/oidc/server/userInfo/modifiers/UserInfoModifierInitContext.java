package cz.muni.ics.oidc.server.userInfo.modifiers;


import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import java.util.Properties;

/**
 * Context for initializing UserInfoModifiers.
 *
 * @author Dominik Baránek <baranek@ics.muni.cz>
 */
public class UserInfoModifierInitContext {

	private final String propertyPrefix;
	private final Properties properties;
	private PerunAdapter perunAdapter;

	public UserInfoModifierInitContext(String propertyPrefix, Properties properties, PerunAdapter perunAdapter) {
		this.propertyPrefix = propertyPrefix;
		this.properties = properties;
		this.perunAdapter = perunAdapter;
	}

	public String getProperty(String suffix, String defaultValue) {
		return properties.getProperty(propertyPrefix + "." + suffix, defaultValue);
	}

	public PerunAdapter getPerunAdapter() {
		return perunAdapter;
	}

	public void setPerunAdapter(PerunAdapter perunAdapter) {
		this.perunAdapter = perunAdapter;
	}

}
