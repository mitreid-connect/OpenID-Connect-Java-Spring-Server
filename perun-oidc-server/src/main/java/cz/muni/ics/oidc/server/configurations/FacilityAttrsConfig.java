package cz.muni.ics.oidc.server.configurations;

import java.util.HashSet;
import java.util.Set;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configuration of Facility attributes
 *
 * @author Dominik Frantisek Bucik <bucik@ics.muni.cz>
 */
@Slf4j
public class FacilityAttrsConfig {

	private String checkGroupMembershipAttr;
	private String registrationURLAttr;
	private String allowRegistrationAttr;
	private String dynamicRegistrationAttr;
	private String voShortNamesAttr;
	private String wayfFilterAttr;
	private String wayfEFilterAttr;
	private String testSpAttr;
	private final Set<String> membershipAttrNames = new HashSet<>();
	private final Set<String> filterAttrNames = new HashSet<>();

	public String getCheckGroupMembershipAttr() {
		return checkGroupMembershipAttr;
	}

	public void setCheckGroupMembershipAttr(String checkGroupMembershipAttr) {
		membershipAttrNames.remove(this.checkGroupMembershipAttr);
		membershipAttrNames.add(checkGroupMembershipAttr);
		this.checkGroupMembershipAttr = checkGroupMembershipAttr;
	}

	public String getRegistrationURLAttr() {
		return registrationURLAttr;
	}

	public void setRegistrationURLAttr(String registrationURLAttr) {
		membershipAttrNames.remove(this.registrationURLAttr);
		membershipAttrNames.add(registrationURLAttr);
		this.registrationURLAttr = registrationURLAttr;
	}

	public String getAllowRegistrationAttr() {
		return allowRegistrationAttr;
	}

	public void setAllowRegistrationAttr(String allowRegistrationAttr) {
		membershipAttrNames.remove(this.allowRegistrationAttr);
		membershipAttrNames.add(allowRegistrationAttr);
		this.allowRegistrationAttr = allowRegistrationAttr;
	}

	public String getDynamicRegistrationAttr() {
		return dynamicRegistrationAttr;
	}

	public void setDynamicRegistrationAttr(String dynamicRegistrationAttr) {
		membershipAttrNames.remove(this.dynamicRegistrationAttr);
		membershipAttrNames.add(dynamicRegistrationAttr);
		this.dynamicRegistrationAttr = dynamicRegistrationAttr;
	}

	public String getVoShortNamesAttr() {
		return voShortNamesAttr;
	}

	public void setVoShortNamesAttr(String voShortNamesAttr) {
		membershipAttrNames.remove(this.voShortNamesAttr);
		membershipAttrNames.add(voShortNamesAttr);
		this.voShortNamesAttr = voShortNamesAttr;
	}

	public String getWayfFilterAttr() {
		return wayfFilterAttr;
	}

	public void setWayfFilterAttr(String wayfFilterAttr) {
		filterAttrNames.remove(this.wayfFilterAttr);
		filterAttrNames.add(wayfFilterAttr);
		this.wayfFilterAttr = wayfFilterAttr;
	}

	public String getWayfEFilterAttr() {
		return wayfEFilterAttr;
	}

	public void setWayfEFilterAttr(String wayfEFilterAttr) {
		filterAttrNames.remove(this.wayfEFilterAttr);
		filterAttrNames.add(wayfEFilterAttr);
		this.wayfEFilterAttr = wayfEFilterAttr;
	}

	public Set<String> getMembershipAttrNames() {
		return membershipAttrNames;
	}

	public Set<String> getFilterAttrNames() {
		return filterAttrNames;
	}

	public String getTestSpAttr() {
		return testSpAttr;
	}

	public void setTestSpAttr(String testSpAttr) {
		this.testSpAttr = testSpAttr;
	}

	@PostConstruct
	public void postInit() {
		log.info("Facility attributes initialized");
		log.info("Check group membership attr mapped to urn: {}", checkGroupMembershipAttr);
		log.info("Allow registration attr mapped to urn: {}", allowRegistrationAttr);
		log.info("Registration URL attr mapped to urn: {}", registrationURLAttr);
		log.info("Allow dynamic registration attr mapped to urn: {}", dynamicRegistrationAttr);
		log.info("Vo short names attr mapped to urn: {}", voShortNamesAttr);
		log.info("IDP Filter attr mapped to urn: {}", wayfFilterAttr);
		log.info("IDP E-Filter attr mapped to urn: {}", wayfEFilterAttr);
		log.info("Test SP attr mapped to urn: {}", testSpAttr);
	}

}
