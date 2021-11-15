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

import static org.junit.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.springframework.web.util.UriComponents;

/**
 * @author wkim
 *
 */
public class TestWebfingerURLNormalizer {


	// Test fixture:
	private ImmutableMap<String, String> inputToNormalized = new ImmutableMap.Builder<String, String>()
			.put("example.com", "https://example.com")
			.put("example.com:8080", "https://example.com:8080")
			.put("example.com/path", "https://example.com/path")
			.put("example.com?query", "https://example.com?query")
			.put("example.com#fragment", "https://example.com")
			.put("example.com:8080/path?query#fragment", "https://example.com:8080/path?query")

			.put("http://example.com", "http://example.com")
			.put("http://example.com:8080", "http://example.com:8080")
			.put("http://example.com/path", "http://example.com/path")
			.put("http://example.com?query", "http://example.com?query")
			.put("http://example.com#fragment", "http://example.com")
			.put("http://example.com:8080/path?query#fragment", "http://example.com:8080/path?query")

			.put("nov@example.com", "acct:nov@example.com")
			.put("nov@example.com:8080", "https://nov@example.com:8080")
			.put("nov@example.com/path", "https://nov@example.com/path")
			.put("nov@example.com?query", "https://nov@example.com?query")
			.put("nov@example.com#fragment", "acct:nov@example.com")
			.put("nov@example.com:8080/path?query#fragment", "https://nov@example.com:8080/path?query")

			.put("acct:nov@matake.jp", "acct:nov@matake.jp")
			.put("acct:nov@example.com:8080", "acct:nov@example.com:8080")
			.put("acct:nov@example.com/path", "acct:nov@example.com/path")
			.put("acct:nov@example.com?query", "acct:nov@example.com?query")
			.put("acct:nov@example.com#fragment", "acct:nov@example.com")
			.put("acct:nov@example.com:8080/path?query#fragment", "acct:nov@example.com:8080/path?query")

			.put("mailto:nov@matake.jp", "mailto:nov@matake.jp")
			.put("mailto:nov@example.com:8080", "mailto:nov@example.com:8080")
			.put("mailto:nov@example.com/path", "mailto:nov@example.com/path")
			.put("mailto:nov@example.com?query", "mailto:nov@example.com?query")
			.put("mailto:nov@example.com#fragment", "mailto:nov@example.com")
			.put("mailto:nov@example.com:8080/path?query#fragment", "mailto:nov@example.com:8080/path?query")

			.put("localhost", "https://localhost")
			.put("localhost:8080", "https://localhost:8080")
			.put("localhost/path", "https://localhost/path")
			.put("localhost?query", "https://localhost?query")
			.put("localhost#fragment", "https://localhost")
			.put("localhost/path?query#fragment", "https://localhost/path?query")
			.put("nov@localhost", "acct:nov@localhost")
			.put("nov@localhost:8080", "https://nov@localhost:8080")
			.put("nov@localhost/path", "https://nov@localhost/path")
			.put("nov@localhost?query", "https://nov@localhost?query")
			.put("nov@localhost#fragment", "acct:nov@localhost")
			.put("nov@localhost/path?query#fragment", "https://nov@localhost/path?query")

			.put("tel:+810312345678", "tel:+810312345678")
			.put("device:192.168.2.1", "device:192.168.2.1")
			.put("device:192.168.2.1:8080", "device:192.168.2.1:8080")
			.put("device:192.168.2.1/path", "device:192.168.2.1/path")
			.put("device:192.168.2.1?query", "device:192.168.2.1?query")
			.put("device:192.168.2.1#fragment", "device:192.168.2.1")
			.put("device:192.168.2.1/path?query#fragment", "device:192.168.2.1/path?query")

			.build();


	/*
	Adapted from Nov Matake's Ruby normalizer implementation.

	 ## INPUT => NORMALIZED
	# example.com => https://example.com
	# example.com:8080 => https://example.com:8080
	# example.com/path => https://example.com/path
	# example.com?query => https://example.com?query
	# example.com#fragment => https://example.com
	# example.com:8080/path?query#fragment => https://example.com:8080/path?query

	# http://example.com => http://example.com
	# http://example.com:8080 => http://example.com:8080
	# http://example.com/path => http://example.com/path
	# http://example.com?query => http://example.com?query
	# http://example.com#fragment => http://example.com
	# http://example.com:8080/path?query#fragment => http://example.com:8080/path?query

	# nov@example.com => acct:nov@example.com
	# nov@example.com:8080 => https://nov@example.com:8080
	# nov@example.com/path => https://nov@example.com/path
	# nov@example.com?query => https://nov@example.com?query
	# nov@example.com#fragment => acct:nov@example.com
	# nov@example.com:8080/path?query#fragment => https://nov@example.com:8080/path?query

	# acct:nov@matake.jp => acct:nov@matake.jp
	# acct:nov@example.com:8080 => acct:nov@example.com:8080
	# acct:nov@example.com/path => acct:nov@example.com/path
	# acct:nov@example.com?query => acct:nov@example.com?query
	# acct:nov@example.com#fragment => acct:nov@example.com
	# acct:nov@example.com:8080/path?query#fragment => acct:nov@example.com:8080/path?query

	# mailto:nov@matake.jp => mailto:nov@matake.jp
	# mailto:nov@example.com:8080 => mailto:nov@example.com:8080
	# mailto:nov@example.com/path => mailto:nov@example.com/path
	# mailto:nov@example.com?query => mailto:nov@example.com?query
	# mailto:nov@example.com#fragment => mailto:nov@example.com
	# mailto:nov@example.com:8080/path?query#fragment => mailto:nov@example.com:8080/path?query

	# localhost => https://localhost
	# localhost:8080 => https://localhost:8080
	# localhost/path => https://localhost/path
	# localhost?query => https://localhost?query
	# localhost#fragment => https://localhost
	# localhost/path?query#fragment => https://localhost/path?query
	# nov@localhost => acct:nov@localhost
	# nov@localhost:8080 => https://nov@localhost:8080
	# nov@localhost/path => https://nov@localhost/path
	# nov@localhost?query => https://nov@localhost?query
	# nov@localhost#fragment => acct:nov@localhost
	# nov@localhost/path?query#fragment => https://nov@localhost/path?query

	# tel:+810312345678 => tel:+810312345678
	# device:192.168.2.1 => device:192.168.2.1
	# device:192.168.2.1:8080 => device:192.168.2.1:8080
	# device:192.168.2.1/path => device:192.168.2.1/path
	# device:192.168.2.1?query => device:192.168.2.1?query
	# device:192.168.2.1#fragment => device:192.168.2.1
	# device:192.168.2.1/path?query#fragment => device:192.168.2.1/path?query

	 *

	 */


	@Test
	public void normalizeResource_novTest() {
		for (String input : inputToNormalized.keySet()) {

			UriComponents actualNormalized = WebfingerURLNormalizer.normalizeResource(input);

			String expectedNormalized = inputToNormalized.get(input);

			assertEquals("Identifer/Normalized failed.", expectedNormalized, WebfingerURLNormalizer.serializeURL(actualNormalized));
		}
	}
}
