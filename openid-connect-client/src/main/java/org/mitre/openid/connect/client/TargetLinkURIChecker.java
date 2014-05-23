package org.mitre.openid.connect.client;

public interface TargetLinkURIChecker {

	/**
	 * Check the parameter to make sure that it's a valid deep-link into this application.
	 * 
	 * @param target
	 * @return
	 */
	public String filter(String target);

}
