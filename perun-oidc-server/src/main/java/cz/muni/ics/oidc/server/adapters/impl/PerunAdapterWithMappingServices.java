package cz.muni.ics.oidc.server.adapters.impl;

import cz.muni.ics.oidc.server.AttributeMappingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * Abstract class containing different mapping services. Adapter implementation should extend this class if
 * the implementation needs to use the mapping service.
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
public abstract class PerunAdapterWithMappingServices {

	@Autowired
	@Qualifier("userAttributesMappingService")
	private AttributeMappingsService userAttributesMappingService;

	@Autowired
	@Qualifier("facilityAttributesMappingService")
	private AttributeMappingsService facilityAttributesMappingService;

	@Autowired
	@Qualifier("groupAttributesMappingService")
	private AttributeMappingsService groupAttributesMappingService;

	@Autowired
	@Qualifier("voAttributesMappingService")
	private AttributeMappingsService voAttributesMappingService;

	@Autowired
	@Qualifier("resourceAttributesMappingService")
	private AttributeMappingsService resourceAttributesMappingService;

	public AttributeMappingsService getUserAttributesMappingService() {
		return userAttributesMappingService;
	}

	public void setUserAttributesMappingService(AttributeMappingsService userAttributesMappingService) {
		this.userAttributesMappingService = userAttributesMappingService;
	}

	public AttributeMappingsService getFacilityAttributesMappingService() {
		return facilityAttributesMappingService;
	}

	public void setFacilityAttributesMappingService(AttributeMappingsService facilityAttributesMappingService) {
		this.facilityAttributesMappingService = facilityAttributesMappingService;
	}

	public AttributeMappingsService getGroupAttributesMappingService() {
		return groupAttributesMappingService;
	}

	public void setGroupAttributesMappingService(AttributeMappingsService groupAttributesMappingService) {
		this.groupAttributesMappingService = groupAttributesMappingService;
	}

	public AttributeMappingsService getVoAttributesMappingService() {
		return voAttributesMappingService;
	}

	public void setVoAttributesMappingService(AttributeMappingsService voAttributesMappingService) {
		this.voAttributesMappingService = voAttributesMappingService;
	}

	public AttributeMappingsService getResourceAttributesMappingService() {
		return resourceAttributesMappingService;
	}

	public void setResourceAttributesMappingService(AttributeMappingsService resourceAttributesMappingService) {
		this.resourceAttributesMappingService = resourceAttributesMappingService;
	}

}
