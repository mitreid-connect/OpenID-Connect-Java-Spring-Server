package cz.muni.ics.oidc.server.userInfo.modifiers;

import cz.muni.ics.oidc.server.userInfo.PerunUserInfo;

/**
 * Interface for all code that needs to modify user info.
 *
 * Configuration of userInfo modifiers:
 * <ul>
 *     <li><b>userInfo.modifiers</b> - comma separated list of names of the userInfo modifiers</li>
 * </ul>
 *
 * Configuration of modifier (replace [name] part with the name defined for the modifier):
 * <ul>
 *     <li><b>userInfo.modifier.[name].class</b> - class the modifier instantiates</li>
 * </ul>
 *
 * @see cz.muni.ics.oidc.server.userInfo.modifiers package for specific modifiers and their configuration
 *
 * @author Dominik Baránek <baranek@ics.muni.cz>
 */
public interface UserInfoModifier {

	/**
	 * Performs modification of UserInfo object. Modification depends on implementation.
	 * ATTENTION: param clientId can be NULL. In that case, implementation should not fail, modification should be
	 * rather skipped.
	 *
	 * @param perunUserInfo UserInfo to be modified
	 * @param clientId Id of client. Can be null.
	 */
	void modify(PerunUserInfo perunUserInfo, String clientId);

}
