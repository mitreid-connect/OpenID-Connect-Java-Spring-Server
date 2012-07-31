package org.mitre.jwt.encryption;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.mitre.jwe.model.Jwe;

import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;


public interface JweEncrypter {
	
	public byte[] encryptKey(Jwe jwe, byte[] cmk) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException;
	
	public byte[] encryptClaims(Jwe jwe, byte[] cik) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException;

	public Jwe encryptAndSign(Jwe jwe) throws NoSuchAlgorithmException, JsonIOException, JsonSyntaxException, IOException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException;
	
	public byte[] generateContentKey(byte[] cmk, int keyDataLen, byte[] type) throws NoSuchAlgorithmException;
	
}
