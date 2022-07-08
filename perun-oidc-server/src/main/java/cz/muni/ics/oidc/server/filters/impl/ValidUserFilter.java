package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oidc.exceptions.ConfigurationException;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.FacilityAttrsConfig;
import cz.muni.ics.oidc.server.configurations.PerunOidcConfig;
import cz.muni.ics.oidc.server.filters.AuthProcFilter;
import cz.muni.ics.oidc.server.filters.AuthProcFilterCommonVars;
import cz.muni.ics.oidc.server.filters.AuthProcFilterInitContext;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedController;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;


/**
 * Filter that decides if the user is valid or not. It checks for membership in specified groups and VOs. In addition,
 * if service identifier is present and can be obtained, it also checks membership in specified groups and VOs based on
 * the environment the service is in.
 *
 * Configuration (replace [name] part with the name defined for the filter):
 * @see cz.muni.ics.oidc.server.filters.AuthProcFilter (basic configuration options)
 * <ul>
 *     <li><b>filter.[name].allEnvGroups</b> - Comma separated list of GROUP IDs the user must be always member of</li>
 *     <li><b>filter.[name].allEnvGroups</b> - Comma separated list of VO IDs the user must be always member of</li>
 *     <li><b>filter.[name].testEnvGroups</b> - Comma separated list of GROUP IDs the user must be member of if service
 *  *         is in the test environment</li>
 *     <li><b>filter.[name].testEnvVos</b> - Comma separated list of VO IDs the user must be member of if service
 *  *         is in the test environment</li>
 *     <li><b>filter.[name].prodEnvGroups</b> - Comma separated list of GROUP IDs the user must be member of if service
 *  *         is in the production environment</li>
 *     <li><b>filter.[name].prodEnvVos</b> - Comma separated list of VO IDs the user must be member of if service
 *         is in the production environment</li>
 * </ul>
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@SuppressWarnings("SqlResolve")
@Slf4j
public class ValidUserFilter extends AuthProcFilter {

	/* CONFIGURATION OPTIONS */
	private static final String ALL_ENV_GROUPS = "allEnvGroups";
	private static final String ALL_ENV_VOS = "allEnvVos";
	private static final String TEST_ENV_GROUPS = "testEnvGroups";
	private static final String TEST_ENV_VOS = "testEnvVos";
	private static final String PROD_ENV_GROUPS = "prodEnvGroups";
	private static final String PROD_ENV_VOS = "prodEnvVos";

	private final Set<Long> allEnvGroups;
	private final Set<Long> allEnvVos;
	private final Set<Long> testEnvGroups;
	private final Set<Long> testEnvVos;
	private final Set<Long> prodEnvGroups;
	private final Set<Long> prodEnvVos;
	/* END OF CONFIGURATION OPTIONS */

	private final PerunAdapter perunAdapter;
	private final FacilityAttrsConfig facilityAttrsConfig;
	private final PerunOidcConfig config;

	public ValidUserFilter(AuthProcFilterInitContext ctx) throws ConfigurationException {
		super(ctx);
		this.perunAdapter = ctx.getPerunAdapterBean();
		this.config = ctx.getPerunOidcConfigBean();
		this.facilityAttrsConfig = ctx.getBeanUtil().getBean(FacilityAttrsConfig.class);

		this.allEnvGroups = getIdsFromParam(ctx, ALL_ENV_GROUPS);
		this.allEnvVos = getIdsFromParam(ctx, ALL_ENV_VOS);
		this.testEnvGroups = getIdsFromParam(ctx, TEST_ENV_GROUPS);
		this.testEnvVos = getIdsFromParam(ctx, TEST_ENV_VOS);
		this.prodEnvGroups = getIdsFromParam(ctx, PROD_ENV_GROUPS);
		this.prodEnvVos = getIdsFromParam(ctx, PROD_ENV_VOS);

		if (allSetsEmpty()) {
			throw new ConfigurationException("All sets are configured to be empty");
		}
	}

	private boolean allSetsEmpty() {
		return allEnvVos.isEmpty() && allEnvGroups.isEmpty()
				&& prodEnvVos.isEmpty() && prodEnvGroups.isEmpty()
				&& testEnvVos.isEmpty() && testEnvGroups.isEmpty();
	}

	@Override
	protected boolean process(HttpServletRequest req, HttpServletResponse res, AuthProcFilterCommonVars params) {
		PerunUser user = params.getUser();
		if (user == null || user.getId() == null) {
			log.debug("{} - skip filter execution: no user provided", getFilterName());
			return true;
		}

		Facility facility = params.getFacility();
		if (facility == null || facility.getId() == null) {
			log.debug("{} - skip filter execution: no facility provided", getFilterName());
			return true;
		}

		if (!checkMemberValidInGroupsAndVos(user, allEnvVos, allEnvGroups)) {
			redirectCannotAccess(res, facility, user, params.getClientIdentifier(), PerunUnapprovedController.UNAPPROVED_NOT_IN_MANDATORY_VOS_GROUPS);
			return false;
		}

		PerunAttributeValue isTestSpAttrValue = perunAdapter.getFacilityAttributeValue(facility.getId(), facilityAttrsConfig.getTestSpAttr());
		boolean testService = false;
		if (isTestSpAttrValue != null) {
			testService = isTestSpAttrValue.valueAsBoolean();
		}

		log.debug("{} - service {} in test env", getFilterName(), (testService ? "is" : "is not"));

		Set<Long> vos = new HashSet<>();
		Set<Long> groups = new HashSet<>();
		String unapprovedMapping;
		if (testService) {
			vos.addAll(testEnvVos);
			groups.addAll(testEnvGroups);
			unapprovedMapping = PerunUnapprovedController.UNAPPROVED_NOT_IN_TEST_VOS_GROUPS;
		} else {
			vos.addAll(prodEnvVos);
			groups.addAll(prodEnvGroups);
			unapprovedMapping = PerunUnapprovedController.UNAPPROVED_NOT_IN_PROD_VOS_GROUPS;
		}
		if (!checkMemberValidInGroupsAndVos(user, vos, groups)) {
			log.info("{} - Redirecting to unapproved page with mapping '{}'", getFilterName(), unapprovedMapping);
			redirectCannotAccess(res, facility, user, params.getClientIdentifier(), unapprovedMapping);
			return false;
		}

		log.info("{} - user satisfies the membership criteria", getFilterName());
		return true;
	}

	private void redirectCannotAccess(HttpServletResponse res, Facility facility, PerunUser user,
									  String clientId, String mapping)
	{
		FiltersUtils.redirectUserCannotAccess(config.getConfigBean().getIssuer(), res, facility, user,
				clientId, facilityAttrsConfig, perunAdapter, mapping);
	}

	private Set<Long> getIdsFromParam(AuthProcFilterInitContext params, String propKey) {
		Set<Long> result = new HashSet<>();

		String prop = params.getProperty(propKey, "");
		if (StringUtils.hasText(prop)) {
			String[] parts = prop.split(",");
			for (String idStr: parts) {
				result.add(Long.parseLong(idStr));
			}
		}

		return result;
	}

	private boolean checkMemberValidInGroupsAndVos(PerunUser user, Set<Long> vos, Set<Long> groups) {
		if (!perunAdapter.isValidMemberInGroupsAndVos(user.getId(), vos, groups)) {
			log.info("{} - user is not member in required set of vos and groups", getFilterName());
			log.debug("{} - user: '{}', vos: '{}', groups: '{}'",
					getFilterName(), user.getId(), vos, groups);
			return false;
		}
		return true;
	}

}
