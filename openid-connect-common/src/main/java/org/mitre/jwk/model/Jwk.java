package org.mitre.jwk.model;

import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;

public interface Jwk {

	public abstract String getAlg();

	public abstract String getKid();

	public abstract String getUse();
	
	public abstract Key getKey() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException;

}