package cz.muni.ics.oidc.server.filters.impl;

import static cz.muni.ics.oidc.server.filters.PerunFilterConstants.SAML_EPUID;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.PerunRequestFilter;
import cz.muni.ics.oidc.server.filters.PerunRequestFilterParams;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.util.StringUtils;


/**
 * Filter for collecting data about login.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * <ul>
 *     <li><b>filter.[name].idpNameAttributeName</b> - Mapping to Request attribute containing name of used
 *         Identity Provider</li>
 *     <li><b>filter.[name].idpEntityIdAttributeName</b> - Mapping to Request attribute containing entity_id of used
 *         Identity Provider</li>
 *     <li><b>filter.[name].statisticsTableName</b> - Name of the table where to store data
 *         (depends on DataSource bean mitreIdStats)</li>
 *     <li><b>filter.[name].identityProvidersMapTableName</b> - Name of the table with mapping of entity_id (IDP)
 *         to idp name (depends on DataSource bean mitreIdStats)
 *     <li><b>filter.[name].serviceProvidersMapTableName</b> - Name of the table with mapping of client_id (SP)
 *         to client name (depends on DataSource bean mitreIdStats)</li>
 * </ul>
 *
 * @author Dominik Baránek <baranek@ics.muni.cz>
 */
@SuppressWarnings("SqlResolve")
@Slf4j
public class ProxyStatisticsFilter extends PerunRequestFilter {

	/* CONFIGURATION OPTIONS */
	private static final String IDP_NAME_ATTRIBUTE_NAME = "idpNameAttributeName";
	private static final String IDP_ENTITY_ID_ATTRIBUTE_NAME = "idpEntityIdAttributeName";
	private static final String STATISTICS_TABLE_NAME = "statisticsTableName";
	private static final String IDENTITY_PROVIDERS_MAP_TABLE_NAME = "identityProvidersMapTableName";
	private static final String SERVICE_PROVIDERS_MAP_TABLE_NAME = "serviceProvidersMapTableName";

	private final String idpNameAttributeName;
	private final String idpEntityIdAttributeName;
	private final String statisticsTableName;
	private final String identityProvidersMapTableName;
	private final String serviceProvidersMapTableName;
	/* END OF CONFIGURATION OPTIONS */

	private final DataSource mitreIdStats;
	private final String filterName;

	public ProxyStatisticsFilter(PerunRequestFilterParams params) {
		super(params);
		BeanUtil beanUtil = params.getBeanUtil();
		this.mitreIdStats = beanUtil.getBean("mitreIdStats", DataSource.class);

		this.idpNameAttributeName = params.getProperty(IDP_NAME_ATTRIBUTE_NAME);
		this.idpEntityIdAttributeName = params.getProperty(IDP_ENTITY_ID_ATTRIBUTE_NAME);
		this.statisticsTableName = params.getProperty(STATISTICS_TABLE_NAME);
		this.identityProvidersMapTableName = params.getProperty(IDENTITY_PROVIDERS_MAP_TABLE_NAME);
		this.serviceProvidersMapTableName = params.getProperty(SERVICE_PROVIDERS_MAP_TABLE_NAME);
		this.filterName = params.getFilterName();
	}

	@Override
	protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) {
		HttpServletRequest request = (HttpServletRequest) req;

		ClientDetailsEntity client = params.getClient();
		if (client == null) {
			log.debug("{} - skip execution: no client provided", filterName);
			return true;
		}

		String clientIdentifier = client.getClientId();
		String clientName = client.getClientName();
		SAMLCredential samlCredential = FiltersUtils.getSamlCredential(request);

		String idpEntityId = samlCredential.getAttributeAsString(idpEntityIdAttributeName);
		idpEntityId = this.changeParamEncoding(idpEntityId);
		String idpName = samlCredential.getAttributeAsString(idpNameAttributeName);
		idpName = this.changeParamEncoding(idpName);
		if (!StringUtils.hasText(idpEntityId) || !StringUtils.hasText(idpName)) {
			log.debug("{} - skip execution: no source IDP provided", filterName);
			return true;
		}

		String userId = samlCredential.getAttributeAsString(SAML_EPUID);
		if (!StringUtils.hasText(userId)) {
			log.debug("{} - skip execution: no user ID available", filterName);
			return true;
		}

		this.insertLogin(idpEntityId, idpName, clientIdentifier, clientName, userId);
		this.logUserLogin(idpEntityId, clientIdentifier, clientName, userId);

		return true;
	}

	private void insertLogin(String idpEntityId, String idpName, String spIdentifier, String spName, String userId) {
		LocalDate date = LocalDate.now();
		String insertLoginQuery = "INSERT INTO " + statisticsTableName + "(day, idpId, spId, user, logins)" +
				" VALUES(?, ?, ?, ?, '1') ON DUPLICATE KEY UPDATE logins = logins + 1";

		try (Connection c = mitreIdStats.getConnection()) {
			insertIdpMap(c, idpEntityId, idpName);
			insertSpMap(c, spIdentifier, spName);
			int idpId = extractIdpId(c, idpEntityId);
			int spId = extractSpId(c, spIdentifier);

			try (PreparedStatement preparedStatement = c.prepareStatement(insertLoginQuery)) {
				preparedStatement.setDate(1, Date.valueOf(date));
				preparedStatement.setInt(2, idpId);
				preparedStatement.setInt(3, spId);
				preparedStatement.setString(4, userId);
				preparedStatement.execute();
				log.trace("{} - login entry stored ({}, {}, {}, {}, {})", filterName, idpEntityId, idpName,
						spIdentifier, spName, userId);
			}
		} catch (SQLException ex) {
			log.warn("{} - caught SQLException", filterName);
			log.debug("{} - details:", filterName, ex);
		}
	}

	private int extractSpId(Connection c, String spIdentifier) throws SQLException {
		String getSpIdQuery = "SELECT * FROM " + serviceProvidersMapTableName + " WHERE identifier= ?";

		try (PreparedStatement preparedStatement = c.prepareStatement(getSpIdQuery)) {
			preparedStatement.setString(1, spIdentifier);
			ResultSet rs = preparedStatement.executeQuery();
			rs.first();
			return rs.getInt("spId");
		}
	}

	private int extractIdpId(Connection c, String idpEntityId) throws SQLException {
		String getIdPIdQuery = "SELECT * FROM " + identityProvidersMapTableName + " WHERE identifier = ?";

		try (PreparedStatement preparedStatement = c.prepareStatement(getIdPIdQuery)) {
			preparedStatement.setString(1, idpEntityId);
			ResultSet rs = preparedStatement.executeQuery();
			rs.first();
			return rs.getInt("idpId");
		}
	}

	private void insertSpMap(Connection c, String spIdentifier, String spName) throws SQLException {
		String insertSpMapQuery = "INSERT INTO " + serviceProvidersMapTableName + "(identifier, name)" +
				" VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?";

		try (PreparedStatement preparedStatement = c.prepareStatement(insertSpMapQuery)) {
			preparedStatement.setString(1, spIdentifier);
			preparedStatement.setString(2, spName);
			preparedStatement.setString(3, spName);
			preparedStatement.execute();
			log.trace("{} - SP map entry inserted", filterName);
		}
	}

	private void insertIdpMap(Connection c, String idpEntityId, String idpName) throws SQLException {
		String insertIdpMapQuery = "INSERT INTO " + identityProvidersMapTableName + "(identifier, name)" +
				" VALUES (?, ?) ON DUPLICATE KEY UPDATE name = ?";

		try (PreparedStatement preparedStatement = c.prepareStatement(insertIdpMapQuery)) {
			preparedStatement.setString(1, idpEntityId);
			preparedStatement.setString(2, idpName);
			preparedStatement.setString(3, idpName);
			preparedStatement.execute();
			log.trace("{} - IdP map entry inserted", filterName);
		}
	}

	private String changeParamEncoding(String original) {
		if (original != null && !original.isEmpty()) {
			byte[] sourceBytes = original.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1);
			return new String(sourceBytes, java.nio.charset.StandardCharsets.UTF_8);
		}

		return null;
	}

	private void logUserLogin(String idpEntityId, String spIdentifier, String spName, String userId) {
		log.info("User identity: {}, service: {}, serviceName: {}, via IdP: {}", userId, spIdentifier,
				spName, idpEntityId);
	}

}
