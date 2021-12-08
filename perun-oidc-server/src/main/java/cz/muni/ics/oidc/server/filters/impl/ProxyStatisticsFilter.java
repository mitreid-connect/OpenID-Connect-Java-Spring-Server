package cz.muni.ics.oidc.server.filters.impl;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.saml.SamlProperties;
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
 *     <li><b>filter.[name].ipdIdColumnName</b> - Name for the column which stores IDs of IdPs in statisticsTable</li>
 *     <li><b>filter.[name].spIdColumnName</b> - Name for the column which stores IDs of SPs in statisticsTable</li>
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
	private static final String IDP_ID_COLUMN_NAME = "idpIdColumnName";
	private static final String SP_ID_COLUMN_NAME = "spIdColumnName";

	private final String idpNameAttributeName;
	private final String idpEntityIdAttributeName;
	private final String statisticsTableName;
	private final String identityProvidersMapTableName;
	private final String serviceProvidersMapTableName;
	private final String idpIdColumnName;
	private final String spIdColumnName;
	/* END OF CONFIGURATION OPTIONS */

	private final DataSource mitreIdStats;
	private final String filterName;
	private final SamlProperties samlProperties;

	public ProxyStatisticsFilter(PerunRequestFilterParams params) {
		super(params);
		BeanUtil beanUtil = params.getBeanUtil();
		this.mitreIdStats = beanUtil.getBean("mitreIdStats", DataSource.class);
		this.samlProperties = beanUtil.getBean(SamlProperties.class);

		this.idpNameAttributeName = params.getProperty(IDP_NAME_ATTRIBUTE_NAME);
		this.idpEntityIdAttributeName = params.getProperty(IDP_ENTITY_ID_ATTRIBUTE_NAME);
		this.statisticsTableName = params.getProperty(STATISTICS_TABLE_NAME);
		this.identityProvidersMapTableName = params.getProperty(IDENTITY_PROVIDERS_MAP_TABLE_NAME);
		this.serviceProvidersMapTableName = params.getProperty(SERVICE_PROVIDERS_MAP_TABLE_NAME);
		this.idpIdColumnName = params.getProperty(IDP_ID_COLUMN_NAME);
		this.spIdColumnName = params.getProperty(SP_ID_COLUMN_NAME);
		this.filterName = params.getFilterName();
	}

	@Override
	protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) {
		HttpServletRequest request = (HttpServletRequest) req;

		ClientDetailsEntity client = params.getClient();
		if (client == null) {
			log.warn("{} - skip execution: no client provided", filterName);
			return true;
		} else if (!StringUtils.hasText(client.getClientId())) {
			log.warn("{} - skip execution: no client identifier provided", filterName);
			return true;
		} else if (!StringUtils.hasText(client.getClientName())) {
			log.warn("{} - skip execution: no client name provided", filterName);
			return true;
		}

		SAMLCredential samlCredential = FiltersUtils.getSamlCredential(request);
		if (samlCredential == null) {
			log.warn("{} - skip execution: no authN object available, cannot extract user identifier and idp identifier",
					filterName);
			return true;
		}
		String userIdentifier = FiltersUtils.getExtLogin(samlCredential, samlProperties.getUserIdentifierAttribute());
		if (!StringUtils.hasText(userIdentifier)) {
			log.warn("{} - skip execution: no user identifier provided", filterName);
			return true;
		} else if (!StringUtils.hasText(samlCredential.getAttributeAsString(idpEntityIdAttributeName))) {
			log.warn("{} - skip execution: no authenticating idp identifier provided", filterName);
			return true;
		} else if (!StringUtils.hasText(samlCredential.getAttributeAsString(idpNameAttributeName))) {
			log.warn("{} - skip execution: no authenticating idp identifier provided", filterName);
			return true;
		}

		String idpEntityId = changeParamEncoding(samlCredential.getAttributeAsString(idpEntityIdAttributeName));
		String idpName = changeParamEncoding(samlCredential.getAttributeAsString(idpNameAttributeName));
		String clientId = client.getClientId();
		String clientName = client.getClientName();

		insertOrUpdateLogin(idpEntityId, idpName, clientId, clientName, userIdentifier);
		logUserLogin(idpEntityId, clientId, clientName, userIdentifier);

		return true;
	}

	private void insertOrUpdateLogin(String idpEntityId, String idpName, String spIdentifier, String spName, String userId) {
		Connection c;
		int idpId;
		int spId;

		try {
			c = mitreIdStats.getConnection();
			insertOrUpdateIdpMap(c, idpEntityId, idpName);
			insertOrUpdateSpMap(c, spIdentifier, spName);

			idpId = extractIdpId(c, idpEntityId);
			spId = extractSpId(c, spIdentifier);
			log.trace("{} - Extracted IDs for SP and IdP: spId={}, idpId ={}", filterName, spId, idpId);
		} catch (SQLException ex) {
			log.warn("{} - caught SQLException", filterName);
			log.debug("{} - details:", filterName, ex);
			return;
		}

		LocalDate date = LocalDate.now();

		try {
			insertLogin(date, c, idpId, spId, userId);
			log.trace("{} - login entry inserted ({}, {}, {}, {}, {})", filterName, idpEntityId, idpName,
					spIdentifier, spName, userId);
		} catch (SQLException ex) {
			try {
				updateLogin(date, c, idpId, spId, userId);
				log.trace("{} - login entry updated ({}, {}, {}, {}, {})", filterName, idpEntityId, idpName,
						spIdentifier, spName, userId);
			} catch (SQLException e) {
				log.warn("{} - caught SQLException", filterName);
				log.debug("{} - details:", filterName, e);
			}
		}
	}

	private int extractSpId(Connection c, String spIdentifier) throws SQLException {
		String query = "SELECT " + spIdColumnName + " FROM " + serviceProvidersMapTableName +
				" WHERE identifier = ? LIMIT 1";

		try (PreparedStatement preparedStatement = c.prepareStatement(query)) {
			preparedStatement.setString(1, spIdentifier);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				return rs.getInt(spIdColumnName);
			} else {
				throw new SQLException("No result found");
			}
		}
	}

	private int extractIdpId(Connection c, String idpEntityId) throws SQLException {
		String query = "SELECT " + idpIdColumnName + " FROM " + identityProvidersMapTableName +
				" WHERE identifier = ? LIMIT 1";

		try (PreparedStatement preparedStatement = c.prepareStatement(query)) {
			preparedStatement.setString(1, idpEntityId);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				return rs.getInt(idpIdColumnName);
			} else {
				throw new SQLException("No result found");
			}
		}
	}

	private void insertOrUpdateIdpMap(Connection c, String idpEntityId, String idpName) throws SQLException {
		try {
			insertIdpMap(c, idpEntityId, idpName);
			log.trace("{} - IdP map entry inserted", filterName);
		} catch (SQLException ex) {
			updateIdpMap(c, idpEntityId, idpName);
			log.trace("{} - IdP map entry updated", filterName);
		}
	}

	private void insertOrUpdateSpMap(Connection c, String spIdentifier, String idpName) throws SQLException {
		try {
			insertSpMap(c, spIdentifier, idpName);
			log.trace("{} - SP map entry inserted", filterName);
		} catch (SQLException ex) {
			updateSpMap(c, spIdentifier, idpName);
			log.trace("{} - SP map entry updated", filterName);
		}
	}

	private String changeParamEncoding(String original) {
		if (original != null && !original.isEmpty()) {
			byte[] sourceBytes = original.getBytes(ISO_8859_1);
			return new String(sourceBytes, UTF_8);
		}

		return null;
	}

	private void logUserLogin(String idpEntityId, String spIdentifier, String spName, String userId) {
		log.info("{} - User identity: {}, service: {}, serviceName: {}, via IdP: {}", filterName, userId, spIdentifier,
				spName, idpEntityId);
	}

	private void insertLogin(LocalDate date, Connection c, int idpId, int spId, String userId) throws SQLException {
		String insertLoginQuery = "INSERT INTO " + statisticsTableName +
			"(day, " + idpIdColumnName + ", " + spIdColumnName + ", user, logins)" +
			" VALUES(?, ?, ?, ?, '1')";

		try (PreparedStatement preparedStatement = c.prepareStatement(insertLoginQuery)) {
			preparedStatement.setDate(1, Date.valueOf(date));
			preparedStatement.setInt(2, idpId);
			preparedStatement.setInt(3, spId);
			preparedStatement.setString(4, userId);
			preparedStatement.execute();
		}
	}

	private void updateLogin(LocalDate date, Connection c, int idpId, int spId, String userId) throws SQLException {
		String updateLoginQuery = "UPDATE " + statisticsTableName + " SET logins = logins + 1" +
			" WHERE day = ? AND " + idpIdColumnName + " = ? AND " + spIdColumnName + " = ? AND user = ?";

		try (PreparedStatement preparedStatement = c.prepareStatement(updateLoginQuery)){
			preparedStatement.setDate(1, Date.valueOf(date));
			preparedStatement.setInt(2, idpId);
			preparedStatement.setInt(3, spId);
			preparedStatement.setString(4, userId);
			preparedStatement.execute();
		}
	}

	private void insertIdpMap(Connection c, String idpEntityId, String idpName) throws SQLException {
		String insertIdpMapQuery = "INSERT INTO " + identityProvidersMapTableName + " (identifier, name)" +
			" VALUES (?, ?)";

		try (PreparedStatement preparedStatement = c.prepareStatement(insertIdpMapQuery)) {
			preparedStatement.setString(1, idpEntityId);
			preparedStatement.setString(2, idpName);
			preparedStatement.execute();
		}
	}

	private void updateIdpMap(Connection c, String idpEntityId, String idpName) throws SQLException {
		String updateIdpMapQuery = "UPDATE " + identityProvidersMapTableName + " SET name = ? WHERE identifier = ?";

		try (PreparedStatement preparedStatement = c.prepareStatement(updateIdpMapQuery)) {
			preparedStatement.setString(1, idpName);
			preparedStatement.setString(2, idpEntityId);
			preparedStatement.execute();
		}
	}

	private void insertSpMap(Connection c, String spIdentifier, String spName) throws SQLException {
		String insertSpMapQuery = "INSERT INTO " + serviceProvidersMapTableName + " (identifier, name)" +
			" VALUES (?, ?)";

		try (PreparedStatement preparedStatement = c.prepareStatement(insertSpMapQuery)) {
			preparedStatement.setString(1, spIdentifier);
			preparedStatement.setString(2, spName);
			preparedStatement.execute();
		}
	}

	private void updateSpMap(Connection c, String spIdentifier, String idpName) throws SQLException {
		String updateSpMapQuery = "UPDATE " + serviceProvidersMapTableName + " SET name = ? WHERE identifier = ?";

		try (PreparedStatement preparedStatement = c.prepareStatement(updateSpMapQuery)) {
			preparedStatement.setString(1, idpName);
			preparedStatement.setString(2, spIdentifier);
			preparedStatement.execute();
		}
	}

}
