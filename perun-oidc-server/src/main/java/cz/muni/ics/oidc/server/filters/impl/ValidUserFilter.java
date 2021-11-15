package cz.muni.ics.oidc.server.filters.impl;

import cz.muni.ics.oidc.BeanUtil;
import cz.muni.ics.oidc.models.Facility;
import cz.muni.ics.oidc.models.PerunAttributeValue;
import cz.muni.ics.oidc.models.PerunUser;
import cz.muni.ics.oidc.server.adapters.PerunAdapter;
import cz.muni.ics.oidc.server.configurations.FacilityAttrsConfig;
import cz.muni.ics.oidc.server.filters.FilterParams;
import cz.muni.ics.oidc.server.filters.FiltersUtils;
import cz.muni.ics.oidc.server.filters.PerunRequestFilter;
import cz.muni.ics.oidc.server.filters.PerunRequestFilterParams;
import cz.muni.ics.oidc.web.controllers.PerunUnapprovedController;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
public class ValidUserFilter extends PerunRequestFilter {

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
	private final String filterName;

	public ValidUserFilter(PerunRequestFilterParams params) {
		super(params);
		BeanUtil beanUtil = params.getBeanUtil();
		this.perunAdapter = beanUtil.getBean(PerunAdapter.class);
		this.facilityAttrsConfig = beanUtil.getBean(FacilityAttrsConfig.class);

		this.allEnvGroups = this.getIdsFromParam(params, ALL_ENV_GROUPS);
		this.allEnvVos = this.getIdsFromParam(params, ALL_ENV_VOS);
		this.testEnvGroups = this.getIdsFromParam(params, TEST_ENV_GROUPS);
		this.testEnvVos = this.getIdsFromParam(params, TEST_ENV_VOS);
		this.prodEnvGroups = this.getIdsFromParam(params, PROD_ENV_GROUPS);
		this.prodEnvVos = this.getIdsFromParam(params, PROD_ENV_VOS);
		this.filterName = params.getFilterName();
	}

	@Override
	protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		Set<Long> additionalVos = new HashSet<>();
		Set<Long> additionalGroups = new HashSet<>();

		PerunUser user = params.getUser();

		if (user == null || user.getId() == null) {
			log.debug("{} - skip filter execution: no user provided", filterName);
			return true;
		}

		Facility facility = params.getFacility();
		if (facility == null || facility.getId() == null) {
			log.debug("{} - skip filter execution: no facility provided", filterName);
			return true;
		}

		if (!checkMemberValidInGroupsAndVos(user, facility, request, response, params, allEnvVos, allEnvGroups,
				PerunUnapprovedController.UNAPPROVED_NOT_IN_MANDATORY_VOS_GROUPS)) {
			return false;
		}

		PerunAttributeValue isTestSp = perunAdapter.getFacilityAttributeValue(facility.getId(), facilityAttrsConfig.getTestSpAttr());
		boolean isTestSpBool = false;
		if (isTestSp != null) {
			isTestSpBool = isTestSp.valueAsBoolean();
		}
		log.debug("{} - service {} in test env", filterName, (isTestSpBool ? "is" : "is not"));
		if (isTestSpBool) {
			additionalVos.addAll(testEnvVos);
			additionalGroups.addAll(testEnvGroups);

			if (!checkMemberValidInGroupsAndVos(user, facility, request, response, params, additionalVos,
					additionalGroups, PerunUnapprovedController.UNAPPROVED_NOT_IN_TEST_VOS_GROUPS)) {
				return false;
			}
		} else {
			additionalVos.addAll(prodEnvVos);
			additionalGroups.addAll(prodEnvGroups);

			if (!checkMemberValidInGroupsAndVos(user, facility, request, response, params, additionalVos,
					additionalGroups, PerunUnapprovedController.UNAPPROVED_NOT_IN_PROD_VOS_GROUPS)) {
				return false;
			}
		}

		log.info("{} - user satisfies the membership criteria", filterName);
		return true;
	}

	private Set<Long> getIdsFromParam(PerunRequestFilterParams params, String propKey) {
		Set<Long> result = new HashSet<>();

		String prop = params.getProperty(propKey);
		if (StringUtils.hasText(prop)) {
			String[] parts = prop.split(",");
			for (String idStr: parts) {
				result.add(Long.parseLong(idStr));
			}
		}

		return result;
	}

	private boolean checkMemberValidInGroupsAndVos(
			PerunUser user,
			Facility facility,
			HttpServletRequest request,
			HttpServletResponse response,
			FilterParams params,
			Set<Long> vos,
			Set<Long> groups,
			String redirectUrl
	) {
		if (!perunAdapter.isValidMemberInGroupsAndVos(user.getId(), vos, groups)) {
			log.info("{} - user is not member in required set of vos and groups", filterName);
			log.debug("{} - user: '{}', vos: '{}', groups: '{}'",
					filterName, user.getId(), vos, groups);

			Map<String, PerunAttributeValue> facilityAttributes = perunAdapter.getFacilityAttributeValues(
					facility, facilityAttrsConfig.getMembershipAttrNames());

			FiltersUtils.redirectUserCannotAccess(request, response, facility, user, params.getClientIdentifier(),
					facilityAttrsConfig, facilityAttributes, perunAdapter, redirectUrl);

			return false;
		}
		return true;
	}

}
