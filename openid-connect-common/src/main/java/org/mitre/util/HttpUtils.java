package org.mitre.util;

import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;

import org.mitre.exception.SystemException;

public class HttpUtils {

	public static URL getHost(HttpServletRequest request) {
		try {
			return new URL(request.getRequestURL().toString());
		} catch (MalformedURLException e) {
			throw new SystemException(e.getMessage(), e);
		}
	}
}
