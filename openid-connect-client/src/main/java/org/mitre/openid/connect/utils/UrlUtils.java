package org.mitre.openid.connect.utils;

import org.mitre.discovery.util.WebfingerURLNormalizer;

public class UrlUtils {

	public static String normalizeIssuerURL(String iss) {
		String issNormalized = WebfingerURLNormalizer.normalizeResource(iss).toString();
		if(!issNormalized.endsWith("/")){
			issNormalized += "/";
		}
		return issNormalized;
	}
}
