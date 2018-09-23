package org.mitre.host.util;

import java.net.URL;

import org.mitre.util.ThreadUtils;

public class HostUtils {

	public static final String CURRENT_HOST_URL_ATTR = "currentHostUrl";
	
	public static void setCurrentHost(URL hostUrl) {
		ThreadUtils.set(CURRENT_HOST_URL_ATTR, hostUrl);
	}
	
	public static URL getCurrentHost() {
		return (URL) ThreadUtils.get(CURRENT_HOST_URL_ATTR);
	}
}
