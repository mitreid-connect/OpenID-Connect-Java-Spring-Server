package org.mitre.openid.connect.utils;

import org.mitre.discovery.util.WebfingerURLNormalizer;

public class UrlUtils {

	/**
	 * @param iss
	 * @return the issuer URL normalized with slash (/) at the end
	 */
	public static String normalizeIssuerURL(String iss) {
		if(iss == null){
			return iss;
		}
		String issNormalized = WebfingerURLNormalizer.normalizeResource(iss).toString();
		if(!issNormalized.endsWith("/")){
			issNormalized += "/";
		}
		return issNormalized;
	}
}
