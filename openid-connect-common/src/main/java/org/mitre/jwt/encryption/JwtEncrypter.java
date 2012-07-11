package org.mitre.jwt.encryption;

import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import org.mitre.jwe.model.Jwe;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public interface JwtEncrypter {
	
	public byte[] encryptKey(Jwe jwe, Key cmk) throws JsonIOException, JsonSyntaxException, IOException;
	
	public byte[] encryptClaims(Jwe jwe, byte[] cik);

	public Jwe encryptAndSign(Jwe jwe) throws NoSuchAlgorithmException, JsonIOException, JsonSyntaxException, IOException;
	
	public byte[] generateContentKey(byte[] cmk, int keyDataLen, byte[] type);
	
	public byte[] intToFourBytes(int i);

}
