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
import java.util.Map;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Authorization filter. Decides if user can access the service based on his/hers
 * membership in the groups assigned to the Perun facility resources. Facility represents
 * client in this context.
 *
 * Configuration:
 * - based on the configuration of bean "facilityAttrsConfig"
 * @see FacilityAttrsConfig
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunAuthorizationFilter extends PerunRequestFilter {

	private final PerunAdapter perunAdapter;
	private final FacilityAttrsConfig facilityAttrsConfig;
	private final String filterName;

	public PerunAuthorizationFilter(PerunRequestFilterParams params) {
		super(params);
		BeanUtil beanUtil = params.getBeanUtil();
		this.perunAdapter = beanUtil.getBean(PerunAdapter.class);
		this.facilityAttrsConfig = beanUtil.getBean(FacilityAttrsConfig.class);
		this.filterName = params.getFilterName();
	}

	@Override
	protected boolean process(ServletRequest req, ServletResponse res, FilterParams params) {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) res;

		Facility facility = params.getFacility();
		if (facility == null || facility.getId() == null) {
			log.debug("{} - skip filter execution: no facility provided", filterName);
			return true;
		}

		PerunUser user = params.getUser();
		if (user == null || user.getId() == null) {
			log.debug("{} - skip filter execution: no user provided", filterName);
			return true;
		}

		return this.decideAccess(facility, user, request, response, params.getClientIdentifier(),
				perunAdapter, facilityAttrsConfig);
	}

	private boolean decideAccess(Facility facility, PerunUser user, HttpServletRequest request,
								 HttpServletResponse response, String clientIdentifier, PerunAdapter perunAdapter,
								 FacilityAttrsConfig facilityAttrsConfig)
	{
		Map<String, PerunAttributeValue> facilityAttributes = perunAdapter.getFacilityAttributeValues(
				facility, facilityAttrsConfig.getMembershipAttrNames());

		if (!facilityAttributes.get(facilityAttrsConfig.getCheckGroupMembershipAttr()).valueAsBoolean()) {
			log.debug("{} - skip filter execution: membership check not requested", filterName);
			return true;
		}

		if (perunAdapter.canUserAccessBasedOnMembership(facility, user.getId())) {
			log.info("{} - user allowed to access the service", filterName);
			return true;
		} else {
			FiltersUtils.redirectUserCannotAccess(request, response, facility, user, clientIdentifier,
					facilityAttrsConfig, facilityAttributes, perunAdapter,
					PerunUnapprovedController.UNAPPROVED_AUTHORIZATION);
			return false;
		}
	}

}
