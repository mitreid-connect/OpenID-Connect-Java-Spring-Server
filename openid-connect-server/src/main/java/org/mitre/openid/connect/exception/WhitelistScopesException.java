/**
 * <copyright>
 * <p>
 * Copyright (c) 2010-2023 Gresham Technologies plc. All rights reserved.
 *
 * </copyright>
 */
package org.mitre.openid.connect.exception;

/**
 * @author hwsmith
 */
public class WhitelistScopesException extends Exception {

	private final String invalidScope;

	public WhitelistScopesException(String invalidScope) {
		this.invalidScope = invalidScope;
	}

	public String getMessage() {
		return "The scope " + invalidScope + " is invalid as it contains non-alphabet characters";
	}

}
