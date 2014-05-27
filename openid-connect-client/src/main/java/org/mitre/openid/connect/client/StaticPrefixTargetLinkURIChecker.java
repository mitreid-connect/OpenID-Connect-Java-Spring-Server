package org.mitre.openid.connect.client;

/**
 * Simple target URI checker, checks whether the string in question starts
 * with a configured prefix. Returns "/" if the match fails.
 * 
 * @author jricher
 *
 */
public class StaticPrefixTargetLinkURIChecker implements TargetLinkURIChecker {

	private String prefix = "";

	@Override
	public String filter(String target) {
		if (target == null) {
			return "/";
		} else if (target.startsWith(prefix)) {
			return target;
		} else {
			return "/";
		}
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

}
