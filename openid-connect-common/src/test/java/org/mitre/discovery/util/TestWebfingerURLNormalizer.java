/*******************************************************************************
 * Copyright 2013 The MITRE Corporation 
 *   and the MIT Kerberos and Internet Trust Consortium
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
 ******************************************************************************/
package org.mitre.discovery.util;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

/**
 * @author wkim
 *
 */
public class TestWebfingerURLNormalizer {


	// Test fixture:

	private List<String> identifiers_noScheme = Lists.newArrayList(
			"example.com",
			"example.com:8080",
			"example.com/path",
			"example.com?query",
			"example.com#fragment",
			"example.com:8080/path?query#fragment");
	
	private List<String> identifiers_http = Lists.newArrayList(
			"http://example.com",
			"http://example.com:8080",
			"http://example.com/path",
			"http://example.com?query",
			"http://example.com#fragment",
			"http://example.com:8080/path?query#fragment");
	
	private List<String> identifiers_noSchemeWithUser = Lists.newArrayList(
			"nov@example.com",
			"nov@example.com:8080",
			"nov@example.com/path",
			"nov@example.com?query",
			"nov@example.com#fragment",
			"nov@example.com:8080/path?query#fragment");
	
	private List<String> identifiers_acct = Lists.newArrayList(
			"acct:nov@matake.jp",
			"acct:nov@example.com:8080",
			"acct:nov@example.com/path",
			"acct:nov@example.com?query",
			"acct:nov@example.com#fragment",
			"acct:nov@example.com:8080/path?query#fragment");
	
	private List<String> identifiers_mailto = Lists.newArrayList(
			"mailto:nov@matake.jp",
			"mailto:nov@example.com:8080",
			"mailto:nov@example.com/path",
			"mailto:nov@example.com?query",
			"mailto:nov@example.com#fragment",
			"mailto:nov@example.com:8080/path?query#fragment");
	
	private List<String> identifiers_localhost = Lists.newArrayList(
			"localhost",
			"localhost:8080",
			"localhost/path",
			"localhost?query",
			"localhost#fragment",
			"localhost/path?query#fragment",
			"nov@localhost",
			"nov@localhost:8080",
			"nov@localhost/path",
			"nov@localhost?query",
			"nov@localhost#fragmentx",
			"nov@localhost/path?query#fragment");
	
	private List<String> identifiers_numbers = Lists.newArrayList(
			"tel:+810312345678",
			"device:192.168.2.1",
			"device:192.168.2.1:8080",
			"device:192.168.2.1/path",
			"device:192.168.2.1?query",
			"device:192.168.2.1#fragment",
			"device:192.168.2.1/path?query#fragment");
	
	
	
	private List<String> expected_noScheme = Lists.newArrayList(
			"https://example.com",
			"https://example.com:8080",
			"https://example.com/path",
			"https://example.com?query",
			"https://example.com",
			"https://example.com:8080/path?query");
	
	private List<String> expected_http = Lists.newArrayList(
			"http://example.com",
			"http://example.com:8080",
			"http://example.com/path",
			"http://example.com?query",
			"http://example.com",
			"http://example.com:8080/path?query");
	
	private List<String> expected_noSchemeWithUser = Lists.newArrayList(
			"acct:nov@example.com",
			"https://nov@example.com:8080",
			"https://nov@example.com/path",
			"https://nov@example.com?query",
			"acct:nov@example.com",
			"https://nov@example.com:8080/path?query");
	
	private List<String> expected_acct = Lists.newArrayList(
			"acct:nov@matake.jp",
			"acct:nov@example.com:8080",
			"acct:nov@example.com/path",
			"acct:nov@example.com?query",
			"acct:nov@example.com",
			"acct:nov@example.com:8080/path?query");
	
	private List<String> expected_mailto = Lists.newArrayList(
			"mailto:nov@matake.jp",
			"mailto:nov@example.com:8080",
			"mailto:nov@example.com/path",
			"mailto:nov@example.com?query",
			"mailto:nov@example.com",
			"mailto:nov@example.com:8080/path?query");
	
	private List<String> expected_localhost = Lists.newArrayList(
			"https://localhost",
			"https://localhost:8080",
			"https://localhost/path",
			"https://localhost?query",
			"https://localhost",
			"https://localhost/path?query",
			"acct:nov@localhost",
			"https://nov@localhost:8080",
			"https://nov@localhost/path",
			"https://nov@localhost?query",
			"https://nov@localhost?query",
			"https://nov@localhost/path?query");
	
	private List<String> expected_numbers = Lists.newArrayList(
			"tel:+810312345678",
			"device:192.168.2.1",
			"device:192.168.2.1:8080",
			"device:192.168.2.1/path",
			"device:192.168.2.1?query",
			"device:192.168.2.1",
			"device:192.168.2.1/path?query");

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
	 */
	
	
	@Test
	public void normalizeResource_noScheme() {
		
		for (int i = 0; i < identifiers_noScheme.size(); i++) {
			
			String identifier = identifiers_noScheme.get(i);
			String expectedIssuer = expected_noScheme.get(i);
			
			String actualIssuer = WebfingerURLNormalizer.normalizeResource(identifier).toUriString();
			
			assertEquals("Identifer/Issuer pair #" + i + " failed.", expectedIssuer, actualIssuer);
		}
	}
	
	@Test
	public void normalizeResource_http() {
		
		for (int i = 0; i < identifiers_http.size(); i++) {
			
			String identifier = identifiers_http.get(i);
			String expectedIssuer = expected_http.get(i);
			
			String actualIssuer = WebfingerURLNormalizer.normalizeResource(identifier).toUriString();
			
			assertEquals("Identifer/Issuer pair #" + i + " failed.", expectedIssuer, actualIssuer);
		}
	}
	
	
	@Test
	public void normalizeResource_noSchemeWithUser() {
		
		for (int i = 0; i < identifiers_noSchemeWithUser.size(); i++) {
			
			String identifier = identifiers_noSchemeWithUser.get(i);
			String expectedIssuer = expected_noSchemeWithUser.get(i);
			
			String actualIssuer = WebfingerURLNormalizer.normalizeResource(identifier).toUriString();
			
			assertEquals("Identifer/Issuer pair #" + i + " failed.", expectedIssuer, actualIssuer);
		}
	}
	
	@Test
	public void normalizeResource_acct() {
		
		for (int i = 0; i < identifiers_acct.size(); i++) {
			
			String identifier = identifiers_acct.get(i);
			String expectedIssuer = expected_acct.get(i);
			
			String actualIssuer = WebfingerURLNormalizer.normalizeResource(identifier).toUriString();
			
			assertEquals("Identifer/Issuer pair #" + i + " failed.", expectedIssuer, actualIssuer);
		}
	}
	
	
	@Test
	public void normalizeResource_mailto() {
		
		for (int i = 0; i < identifiers_mailto.size(); i++) {
			
			String identifier = identifiers_mailto.get(i);
			String expectedIssuer = expected_mailto.get(i);
			
			String actualIssuer = WebfingerURLNormalizer.normalizeResource(identifier).toUriString();
			
			assertEquals("Identifer/Issuer pair #" + i + " failed.", expectedIssuer, actualIssuer);
		}
	}
	
	@Test
	public void normalizeResource_localhost() {
		
		for (int i = 0; i < identifiers_localhost.size(); i++) {
			
			String identifier = identifiers_localhost.get(i);
			String expectedIssuer = expected_localhost.get(i);
			
			String actualIssuer = WebfingerURLNormalizer.normalizeResource(identifier).toUriString();
			
			assertEquals("Identifer/Issuer pair #" + i + " failed.", expectedIssuer, actualIssuer);
		}
	}
	
	@Test
	public void normalizeResource_numbers() {
		
		for (int i = 0; i < identifiers_numbers.size(); i++) {
			
			String identifier = identifiers_numbers.get(i);
			String expectedIssuer = expected_numbers.get(i);
			
			String actualIssuer = WebfingerURLNormalizer.normalizeResource(identifier).toUriString();
			
			assertEquals("Identifer/Issuer pair #" + i + " failed.", expectedIssuer, actualIssuer);
		}
	}
}
