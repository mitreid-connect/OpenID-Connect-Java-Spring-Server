package cz.muni.ics.oidc.server.filters.impl;

import static cz.muni.ics.oidc.web.controllers.PerunUnapprovedController.UNAPPROVED_AUTHORIZATION;

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
import java.util.Map;
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
 * @see cz.muni.ics.oidc.server.filters.AuthProcFilter (basic configuration options)
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class PerunAuthorizationFilter extends AuthProcFilter {

	private final PerunAdapter perunAdapter;
	private final FacilityAttrsConfig facilityAttrsConfig;
	private final PerunOidcConfig config;

	public PerunAuthorizationFilter(AuthProcFilterInitContext ctx) throws ConfigurationException {
		super(ctx);
		this.perunAdapter = ctx.getPerunAdapterBean();
		this.config = ctx.getPerunOidcConfigBean();
		this.facilityAttrsConfig = ctx.getBeanUtil().getBean(FacilityAttrsConfig.class);
	}

	@Override
	protected boolean process(HttpServletRequest req, HttpServletResponse res, AuthProcFilterCommonVars params) {
		Facility facility = params.getFacility();
		if (facility == null || facility.getId() == null) {
			log.debug("{} - skip filter execution: no facility provided", getFilterName());
			return true;
		}

		PerunUser user = params.getUser();
		if (user == null || user.getId() == null) {
			log.debug("{} - skip filter execution: no user provided", getFilterName());
			return true;
		}

		return this.decideAccess(facility, user, req, res, params.getClientIdentifier(),
				perunAdapter, facilityAttrsConfig);
	}

	private boolean decideAccess(Facility facility, PerunUser user, HttpServletRequest request,
								 HttpServletResponse response, String clientIdentifier, PerunAdapter perunAdapter,
								 FacilityAttrsConfig facilityAttrsConfig)
	{
		Map<String, PerunAttributeValue> facilityAttributes = perunAdapter.getFacilityAttributeValues(
				facility, facilityAttrsConfig.getMembershipAttrNames());

		if (!facilityAttributes.get(facilityAttrsConfig.getCheckGroupMembershipAttr()).valueAsBoolean()) {
			log.debug("{} - skip filter execution: membership check not requested", getFilterName());
			return true;
		}

		if (perunAdapter.canUserAccessBasedOnMembership(facility, user.getId())) {
			log.info("{} - user allowed to access the service", getFilterName());
			return true;
		} else {
			FiltersUtils.redirectUserCannotAccess(config.getConfigBean().getIssuer(), response, facility, user,
					clientIdentifier, facilityAttrsConfig, perunAdapter, UNAPPROVED_AUTHORIZATION);
			return false;
		}
	}

}
