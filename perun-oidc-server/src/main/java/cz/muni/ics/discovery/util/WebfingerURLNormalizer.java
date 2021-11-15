/*******************************************************************************
 * Copyright 2018 The MIT Internet Trust Consortium
 *
 * Portions copyright 2011-2013 The MITRE Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package cz.muni.ics.discovery.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import com.google.common.base.Strings;

/**
 * Provides utility methods for normalizing and parsing URIs for use with Webfinger Discovery.
 *
 * @author wkim
 */
@Slf4j
public class WebfingerURLNormalizer {

	// pattern used to parse user input; we can't use the built-in java URI parser
	private static final Pattern pattern = Pattern.compile("^" +
			"((https|acct|http|mailto|tel|device):(//)?)?" + // scheme
			"(" +
			"(([^@]+)@)?" + // userinfo
			"(([^\\?#:/]+)" + // host
			"(:(\\d*))?)" + // port
			")" +
			"([^\\?#]+)?" + // path
			"(\\?([^#]+))?" + // query
			"(#(.*))?" +  // fragment
			"$"
			);

	private WebfingerURLNormalizer() { }

	/**
	 * Normalize the resource string as per OIDC Discovery.
	 * @param identifier
	 * @return the normalized string, or null if the string can't be normalized
	 */
	public static UriComponents normalizeResource(String identifier) {
		// try to parse the URI
		// NOTE: we can't use the Java built-in URI class because it doesn't split the parts appropriately

		if (StringUtils.isEmpty(identifier)) {
			log.warn("Can't normalize null or empty URI: " + identifier);
			return null;
		} else {
			UriComponentsBuilder builder = UriComponentsBuilder.newInstance();

			Matcher m = pattern.matcher(identifier);
			if (m.matches()) {
				builder.scheme(m.group(2));
				builder.userInfo(m.group(6));
				builder.host(m.group(8));
				String port = m.group(10);
				if (!StringUtils.isEmpty(port)) {
					builder.port(Integer.parseInt(port));
				}
				builder.path(m.group(11));
				builder.query(m.group(13));
				builder.fragment(m.group(15)); // we throw away the hash, but this is the group it would be if we kept it
			} else {
				log.warn("Parser couldn't match input: {}", identifier);
				return null;
			}

			UriComponents n = builder.build();

			if (Strings.isNullOrEmpty(n.getScheme())) {
				if (!Strings.isNullOrEmpty(n.getUserInfo())
						&& Strings.isNullOrEmpty(n.getPath())
						&& Strings.isNullOrEmpty(n.getQuery())
						&& n.getPort() < 0) {

					// scheme empty, userinfo is not empty, path/query/port are empty
					// set to "acct" (rule 2)
					builder.scheme("acct");
				} else {
					// scheme is empty, but rule 2 doesn't apply
					// set scheme to "https" (rule 3)
					builder.scheme("https");
				}
			}

			// fragment must be stripped (rule 4)
			builder.fragment(null);

			return builder.build();
		}
	}

	public static String serializeURL(UriComponents uri) {
		String scheme = uri.getScheme();
		if (scheme != null &&
				(scheme.equals("acct") || scheme.equals("mailto") || scheme.equals("tel") || scheme.equals("device"))) {
			// serializer copied from HierarchicalUriComponents but with "//" removed
			StringBuilder uriBuilder = new StringBuilder();

			uriBuilder.append(scheme);
			uriBuilder.append(':');

			String userInfo = uri.getUserInfo();
			String host = uri.getHost();
			if (userInfo != null || host != null) {
				if (userInfo != null) {
					uriBuilder.append(userInfo);
					uriBuilder.append('@');
				}

				if (host != null) {
					uriBuilder.append(host);
				}

				int port = uri.getPort();
				if (port != -1) {
					uriBuilder.append(':');
					uriBuilder.append(port);
				}
			}

			String path = uri.getPath();
			if (StringUtils.hasLength(path)) {
				if (uriBuilder.length() != 0 && path.charAt(0) != '/') {
					uriBuilder.append('/');
				}
				uriBuilder.append(path);
			}

			String query = uri.getQuery();
			if (query != null) {
				uriBuilder.append('?');
				uriBuilder.append(query);
			}

			String fragment = uri.getFragment();
			if (fragment != null) {
				uriBuilder.append('#');
				uriBuilder.append(fragment);
			}

			return uriBuilder.toString();
		} else {
			return uri.toUriString();
		}
	}

}
