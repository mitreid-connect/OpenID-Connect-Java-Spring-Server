package cz.muni.ics.oidc.server.configurations;

import cz.muni.ics.openid.connect.config.ConfigurationPropertiesBean;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Configuration of OIDC server in context of Perun.
 * Logs some interesting facts.
 *
 * @author Martin Kuba <makub@ics.muni.cz>
 */
@Slf4j
public class PerunOidcConfig {

	private static final String OIDC_POM_FILE = "/META-INF/maven/cz.muni.ics/perun-oidc-server-webapp/pom.properties";

	private ConfigurationPropertiesBean configBean;
	private String rpcUrl;
	private String jwk;
	private String jdbcUrl;
	private String theme;
	private String baseURL;
	private String registrarUrl;
	private String samlLoginURL;
	private String samlLogoutURL;
	private String samlResourcesURL;
	private boolean askPerunForIdpFiltersEnabled;
	private String perunOIDCVersion;
	private String proxyExtSourceName;
	private Set<String> idTokenScopes;
	private List<String> availableLangs;
	private boolean fillMissingUserAttrs;
	private boolean addClientIdToAcrs = false;

	@Autowired
	private ServletContext servletContext;

	@Autowired
	private Properties coreProperties;
	private String localizationFilesPath;
	private String webClassesFilePath;
	private String emailContact;
	private String rpcEnabled;

	public void setRpcUrl(String rpcUrl) {
		this.rpcUrl = rpcUrl;
	}

	public void setConfigBean(ConfigurationPropertiesBean configBean) {
		this.configBean = configBean;
	}

	public void setJwk(String jwk) {
		this.jwk = jwk;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public void setTheme(String theme) {
		this.theme = theme;
	}

	public String getTheme() {
		return theme;
	}

	public String getBaseURL() {
		return baseURL;
	}

	public void setBaseURL(String baseURL) {
		this.baseURL = baseURL;
	}

	public String getSamlResourcesURL() {
		return samlResourcesURL;
	}

	public void setSamlResourcesURL(String samlResourcesURL) {
		this.samlResourcesURL = samlResourcesURL;
	}

	public String getRegistrarUrl() {
		return registrarUrl;
	}

	public void setRegistrarUrl(String registrarUrl) {
		this.registrarUrl = registrarUrl;
	}

	public void setIdTokenScopes(Set<String> idTokenScopes) {
		this.idTokenScopes = idTokenScopes;
	}

	public Set<String> getIdTokenScopes() {
		return idTokenScopes;
	}

	public String getPerunOIDCVersion() {
		if (perunOIDCVersion == null) {
			perunOIDCVersion = readPomVersion(OIDC_POM_FILE);
		}
		return perunOIDCVersion;
	}

	private String readPomVersion(String file) {
		try {
			Properties p = new Properties();
			p.load(servletContext.getResourceAsStream(file));
			return p.getProperty("version");
		} catch (IOException e) {
			log.error("cannot read file " + file, e);
			return "UNKNOWN";
		}
	}

	public void setSamlLoginURL(String samlLoginURL) {
		this.samlLoginURL = samlLoginURL;
	}

	public String getSamlLoginURL() {
		return samlLoginURL;
	}

	public void setSamlLogoutURL(String samlLogoutURL) {
		this.samlLogoutURL = samlLogoutURL;
	}

	public String getSamlLogoutURL() {
		return samlLogoutURL;
	}

	public ConfigurationPropertiesBean getConfigBean() {
		return configBean;
	}

	public boolean isAskPerunForIdpFiltersEnabled() {
		return askPerunForIdpFiltersEnabled;
	}

	public void setAskPerunForIdpFiltersEnabled(boolean askPerunForIdpFiltersEnabled) {
		this.askPerunForIdpFiltersEnabled = askPerunForIdpFiltersEnabled;
	}

	public String getProxyExtSourceName() {
		return proxyExtSourceName;
	}

	public void setProxyExtSourceName(String proxyExtSourceName) {
		if (proxyExtSourceName == null || proxyExtSourceName.isEmpty()) {
			this.proxyExtSourceName = null;
		} else {
			this.proxyExtSourceName = proxyExtSourceName;
		}
	}

	public List<String> getAvailableLangs() {
		return availableLangs;
	}

	public void setAvailableLangs(List<String> availableLangs) {
		this.availableLangs = availableLangs;
	}

	public String getLocalizationFilesPath() {
		return localizationFilesPath;
	}

	public void setLocalizationFilesPath(String localizationFilesPath) {
		this.localizationFilesPath = localizationFilesPath;
	}

	public boolean isFillMissingUserAttrs() {
		return fillMissingUserAttrs;
	}

	public void setFillMissingUserAttrs(boolean fillMissingUserAttrs) {
		this.fillMissingUserAttrs = fillMissingUserAttrs;
	}

	public String getWebClassesFilePath() {
		return webClassesFilePath;
	}

	public void setWebClassesFilePath(String webClassesFilePath) {
		this.webClassesFilePath = webClassesFilePath;
	}

	public String getEmailContact() {
		return emailContact;
	}

	public void setEmailContact(String emailContact) {
		this.emailContact = emailContact;
	}


	public void setRpcEnabled(String rpcEnabled) {
		this.rpcEnabled = rpcEnabled;
	}

	public String getRpcEnabled() {
		return rpcEnabled;
	}

	public boolean isAddClientIdToAcrs() {
		return addClientIdToAcrs;
	}

	public void setAddClientIdToAcrs(boolean addClientIdToAcrs) {
		this.addClientIdToAcrs = addClientIdToAcrs;
	}

	@PostConstruct
	public void postInit() {
		//load URLs from properties if available or construct them from issuer URL
		if (samlLoginURL != null && !samlLoginURL.trim().isEmpty()) {
			samlLoginURL = samlLoginURL.trim();
		} else {
			samlLoginURL = UriComponentsBuilder.fromHttpUrl(configBean.getIssuer()).replacePath("/Shibboleth.sso/Login").build().toString();
		}

		if (samlLogoutURL != null && !samlLogoutURL.trim().isEmpty()) {
			samlLogoutURL = samlLogoutURL.trim();
		} else {
			samlLogoutURL = UriComponentsBuilder.fromHttpUrl(configBean.getIssuer()).replacePath("/Shibboleth.sso/Logout").build().toString();
		}

		if (samlResourcesURL != null && !samlResourcesURL.trim().isEmpty()) {
			samlResourcesURL = samlResourcesURL.trim();
		} else {
			samlResourcesURL = UriComponentsBuilder.fromHttpUrl(configBean.getIssuer()).replacePath("/proxy").build().toString();
		}
	}

	//called when all beans are initialized, but twice, once for root context and once for spring-servlet
	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		if (event.getApplicationContext().getParent() == null) {
			//log info
			log.info("Perun OIDC initialized");
			log.info("Mitreid config URL: {}", configBean.getIssuer());
			log.info("RPC URL: {}", rpcUrl);
			log.info("JSON Web Keys: {}", jwk);
			log.info("JDBC URL: {}", jdbcUrl);
			log.info("LDAP: ldaps://{}/{}", coreProperties.getProperty("ldap.host"), coreProperties.getProperty("ldap.baseDN"));
			log.info("FILL MISSING USER ATTRS: {}", fillMissingUserAttrs);
			log.info("THEME: {}", theme);
			log.info("baseURL: {}", baseURL);
			log.info("LOGIN  URL: {}", samlLoginURL);
			log.info("LOGOUT URL: {}", samlLogoutURL);
			log.info("samlResourcesURL: {}", samlResourcesURL);
			log.info("Registrar URL: {}", registrarUrl);
			log.info("accessTokenClaimsModifier: {}", coreProperties.getProperty("accessTokenClaimsModifier"));
			log.info("Proxy EXT_SOURCE name: {}", proxyExtSourceName);
			log.info("Available languages: {}", availableLangs);
			log.info("Localization files path: {}", localizationFilesPath);
			log.info("Email contact: {}", emailContact);
			log.info("Perun OIDC version: {}", getPerunOIDCVersion());
		}
	}

}
