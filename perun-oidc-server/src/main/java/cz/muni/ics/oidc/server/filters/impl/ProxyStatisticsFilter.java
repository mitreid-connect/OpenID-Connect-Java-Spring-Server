package cz.muni.ics.oidc.server.filters.impl;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.UTF_8;

import cz.muni.ics.oauth2.model.ClientDetailsEntity;
import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.saml.SamlProperties;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.AuthProcFilter;
import cz.muni.ics.oidc.server.filters.PerunRequestFilterParams;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Objects;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
 *     <li><b>filter.[name].usernameColumnName</b> - Name for the column which stores IDs of users in statisticsTable</li>
 * </ul>
 *
 * @author Dominik Baránek <baranek@ics.muni.cz>
 */
@SuppressWarnings("SqlResolve")
@Slf4j
public class ProxyStatisticsFilter extends AuthProcFilter {

	public static final String APPLIED = "APPLIED_" + ProxyStatisticsFilter.class.getSimpleName();

	/* CONFIGURATION OPTIONS */
	private static final String IDP_NAME_ATTRIBUTE_NAME = "idpNameAttributeName";
	private static final String IDP_ENTITY_ID_ATTRIBUTE_NAME = "idpEntityIdAttributeName";
	private static final String STATISTICS_TABLE_NAME = "statisticsTableName";
	private static final String IDENTITY_PROVIDERS_MAP_TABLE_NAME = "identityProvidersMapTableName";
	private static final String SERVICE_PROVIDERS_MAP_TABLE_NAME = "serviceProvidersMapTableName";
	private static final String IDP_ID_COLUMN_NAME = "idpIdColumnName";
	private static final String SP_ID_COLUMN_NAME = "spIdColumnName";
	private static final String USERNAME_COLUMN_NAME = "usernameColumnName";

	private final String idpNameAttributeName;
	private final String idpEntityIdAttributeName;
	private final String statisticsTableName;
	private final String identityProvidersMapTableName;
	private final String serviceProvidersMapTableName;
	private final String idpIdColumnName;
	private final String spIdColumnName;
	private final String usernameColumnName;
	/* END OF CONFIGURATION OPTIONS */

	private final DataSource mitreIdStats;
	private final String filterName;
	private final SamlProperties samlProperties;

	public ProxyStatisticsFilter(PerunRequestFilterParams params) {
		super(params);
		BeanUtil beanUtil = params.getBeanUtil();
		this.mitreIdStats = beanUtil.getBean("mitreIdStats", DataSource.class);
		this.samlProperties = beanUtil.getBean(SamlProperties.class);

		this.idpNameAttributeName = params.getProperty(IDP_NAME_ATTRIBUTE_NAME,
				"urn:cesnet:proxyidp:attribute:sourceIdPName");
		this.idpEntityIdAttributeName = params.getProperty(IDP_ENTITY_ID_ATTRIBUTE_NAME,
				"urn:cesnet:proxyidp:attribute:sourceIdPEntityID");
		this.statisticsTableName = params.getProperty(STATISTICS_TABLE_NAME, "statistics_per_user");
		this.identityProvidersMapTableName = params.getProperty(IDENTITY_PROVIDERS_MAP_TABLE_NAME, "statistics_idp");
		this.serviceProvidersMapTableName = params.getProperty(SERVICE_PROVIDERS_MAP_TABLE_NAME, "statistics_sp");
		this.idpIdColumnName = params.getProperty(IDP_ID_COLUMN_NAME, "idpId");
		this.spIdColumnName = params.getProperty(SP_ID_COLUMN_NAME, "spId");
		this.usernameColumnName = params.getProperty(USERNAME_COLUMN_NAME, "user");
		this.filterName = params.getFilterName();
	}

	@Override
	protected String getSessionAppliedParamName() {
		return APPLIED;
	}

	@Override
	protected boolean process(HttpServletRequest req, HttpServletResponse res, FilterParams params) {
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

		SAMLCredential samlCredential = FiltersUtils.getSamlCredential(req);
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

		log.info("{} - User identity: {}, service: {}, serviceName: {}, via IdP: {}",
				filterName, userIdentifier, client.getClientId(), client.getClientName(), idpEntityId);
		return true;
	}

	private void insertOrUpdateLogin(String idpEntityId, String idpName, String spIdentifier, String spName, String userId) {
		try (Connection c = mitreIdStats.getConnection()) {
			insertOrUpdateIdpMap(c, idpEntityId, idpName);
			insertOrUpdateSpMap(c, spIdentifier, spName);

			Long idpId = extractIdpId(c, idpEntityId);
			if (idpId == null) {
				return;
			}
			Long spId = extractSpId(c, spIdentifier);
			if (spId == null) {
				return;
			}
			log.trace("{} - Extracted IDs for SP and IdP: spId={}({}), idpId={}({})",
					filterName, spId, spIdentifier, idpId, idpEntityId);
			insertOrUpdateLogin(c, idpId, spId, userId);
		} catch (SQLException ex) {
			log.warn("{} - caught SQLException", filterName);
			log.debug("{} - details:", filterName, ex);
		}
	}

	private void insertOrUpdateLogin(Connection c, Long idpId, Long spId, String userId) {
		boolean present = fetchLogin(c, idpId, spId, userId);
		if (!present) {
			insertLogin(c, idpId, spId, userId);
		} else {
			updateLogin(c, idpId, spId, userId);
		}
	}

	private boolean fetchLogin(Connection c, Long idpId, Long spId, String userId) {
		String query = "SELECT COUNT(*) AS res FROM " + statisticsTableName +
				" WHERE " + idpIdColumnName + " = ?" +
				" AND " + spIdColumnName + " = ?" +
				" AND " + usernameColumnName + " = ?" +
				" AND day = ?";

		try (PreparedStatement ps = c.prepareStatement(query)) {
			ps.setLong(1, idpId);
			ps.setLong(2, spId);
			ps.setString(3, userId);
			ps.setDate(4, Date.valueOf(LocalDate.now()));
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getInt("res") > 0;
			}
		} catch (SQLException e) {
			log.warn("{} - caught SQLException when fetching login entry", filterName);
			log.debug("{} - details:", filterName, e);
		}
		return false;
	}

	private Long extractSpId(Connection c, String spIdentifier) throws SQLException {
		String query = "SELECT " + spIdColumnName + " FROM " + serviceProvidersMapTableName +
				" WHERE identifier = ? LIMIT 1";

		try (PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, spIdentifier);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong(spIdColumnName);
			}
		}  catch (SQLException ex) {
			log.warn("{} - caught SQLException when extracting SP ID",  filterName);
			log.debug("{} - details:", filterName, ex);
		}
		return null;
	}

	private Long extractIdpId(Connection c, String idpEntityId) throws SQLException {
		String query = "SELECT " + idpIdColumnName + " FROM " + identityProvidersMapTableName +
				" WHERE identifier = ? LIMIT 1";

		try (PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, idpEntityId);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getLong(idpIdColumnName);
			}
		} catch (SQLException ex) {
			log.warn("{} - caught SQLException when extracting IdP ID", filterName);
			log.debug("{} - details:", filterName, ex);
		}
		return null;
	}

	private void insertOrUpdateIdpMap(Connection c, String idpEntityId, String idpName) throws SQLException {
		String idpNameInDb = fetchIdpMapEntry(c, idpEntityId);
		if (!Objects.equals(idpName, idpNameInDb)) {
			if (idpNameInDb == null) {
				if (insertIdpMap(c, idpEntityId, idpName)) {
					log.trace("{} - IdP map entry inserted", filterName);
				}
			} else {
				if (updateIdpMap(c, idpEntityId, idpName)) {
					log.trace("{} - IdP map entry updated", filterName);
				}
			}
		}
	}

	private String fetchIdpMapEntry(Connection c, String idpEntityId) {
		return fetchName(c, idpEntityId, identityProvidersMapTableName);
	}

	private String fetchSpMapEntry(Connection c, String spIdentifier) {
		return fetchName(c, spIdentifier, serviceProvidersMapTableName);
	}

	private String fetchName(Connection c, String entityIdentifier, String table) {
		String query = "SELECT name FROM " + table + " WHERE identifier = ?";
		try (PreparedStatement ps = c.prepareStatement(query)) {
			ps.setString(1, entityIdentifier);
			ResultSet rs = ps.executeQuery();
			if (rs.next()) {
				return rs.getString("name");
			} else {
				return null;
			}
		} catch (SQLException e) {
			return null;
		}
	}

	private void insertOrUpdateSpMap(Connection c, String spIdentifier, String spName) throws SQLException {
		String spNameInDb = fetchSpMapEntry(c, spIdentifier);
		if (!Objects.equals(spName, spNameInDb)) {
			if (spNameInDb == null) {
				if (insertSpMap(c, spIdentifier, spName)) {
					log.trace("{} - SP map entry inserted", filterName);
				}
			} else {
				if (updateSpMap(c, spIdentifier, spName)) {
					log.trace("{} - SP map entry updated", filterName);
				}
			}
		}
	}

	private String changeParamEncoding(String original) {
		if (original != null && !original.isEmpty()) {
			byte[] sourceBytes = original.getBytes(ISO_8859_1);
			return new String(sourceBytes, UTF_8);
		}

		return null;
	}

	private void insertLogin(Connection c, Long idpId, Long spId, String userId) {
		String insertLoginQuery = "INSERT INTO " + statisticsTableName +
			"(day, " + idpIdColumnName + ", " + spIdColumnName + ", " + usernameColumnName + ", logins)" +
			" VALUES(?, ?, ?, ?, '1')";

		try (PreparedStatement ps = c.prepareStatement(insertLoginQuery)) {
			ps.setDate(1, Date.valueOf(LocalDate.now()));
			ps.setLong(2, idpId);
			ps.setLong(3, spId);
			ps.setString(4, userId);
			ps.execute();
			log.debug("{} - Inserted first login for combination: idpId={}, spId={}, userId={}",
					filterName, idpId, spId, userId);
		} catch (SQLException ex) {
			log.warn("{} - caught SQLException when inserting login entry",  filterName);
			log.debug("{} - details:", filterName, ex);
		}
	}

	private void updateLogin(Connection c, Long idpId, Long spId, String userId) {
		String updateLoginQuery = "UPDATE " + statisticsTableName +
				" SET logins = logins + 1" +
				" WHERE day = ?" +
				" AND " + idpIdColumnName + " = ?" +
				" AND " + spIdColumnName + " = ?" +
				" AND " + usernameColumnName + " = ?";

		try (PreparedStatement ps = c.prepareStatement(updateLoginQuery)){
			ps.setDate(1, Date.valueOf(LocalDate.now()));
			ps.setLong(2, idpId);
			ps.setLong(3, spId);
			ps.setString(4, userId);
			log.debug("{} - Updated login count by 1 for combination: idpId={}, spId={}, userId={}",
					filterName, idpId, spId, userId);
		} catch (SQLException ex) {
			log.warn("{} - caught SQLException when updating login entry",  filterName);
			log.debug("{} - details:", filterName, ex);
		}
	}

	private boolean insertIdpMap(Connection c, String idpEntityId, String idpName) {
		return insertIntoMap(c, idpEntityId, idpName, identityProvidersMapTableName);
	}

	private boolean insertSpMap(Connection c, String spIdentifier, String spName) {
		return insertIntoMap(c, spIdentifier, spName, serviceProvidersMapTableName);
	}

	private boolean insertIntoMap(Connection c, String identifier, String name, String table) {
		String insertIdpMapQuery = "INSERT INTO " + table + " (identifier, name)" +
				" VALUES (?, ?)";

		try (PreparedStatement ps = c.prepareStatement(insertIdpMapQuery)) {
			ps.setString(1, identifier);
			ps.setString(2, name);
			ps.execute();
			log.debug("{} - {} entry inserted", filterName, table);
			return true;
		}  catch (SQLException ex) {
			// someone has already inserted it
			log.trace("{} - {} entry failed to insert", filterName, table);
			log.trace("{} - details", filterName, ex);
		}
		return false;
	}

	private boolean updateIdpMap(Connection c, String idpEntityId, String idpName) {
		return updateInMap(c, idpEntityId, idpName, identityProvidersMapTableName);
	}

	private boolean updateSpMap(Connection c, String spIdentifier, String spName) {
		return updateInMap(c, spIdentifier, spName, serviceProvidersMapTableName);
	}

	private boolean updateInMap(Connection c, String identifier, String name, String table) {
		String updateSpMapQuery = "UPDATE " + table + " SET name = ? WHERE identifier = ?";

		try (PreparedStatement ps = c.prepareStatement(updateSpMapQuery)) {
			ps.setString(1, name);
			ps.setString(2, identifier);
			ps.execute();
			log.debug("{} - {} entry updated", filterName, table);
			return true;
		} catch (SQLException ex) {
			log.trace("{} - {} map entry failed to update", filterName, table);
			log.trace("{} - details", filterName);
		}
		return false;
	}

}
