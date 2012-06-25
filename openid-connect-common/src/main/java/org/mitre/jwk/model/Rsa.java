package org.mitre.jwk.model;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonObject;

public class Rsa extends AbstractJwk{
	
	public static final String MODULUS = "mod";
	public static final String EXPONENT = "exp";
	
	private String mod;
	private String exp;
	
	JsonObject object = new JsonObject();
	
	public String getMod() {
		return mod;
	}

	public void setMod(String mod) {
		this.mod = mod;
	}

	public String getExp() {
		return exp;
	}

	public void setExp(String exp) {
		this.exp = exp;
	}

	public Rsa(JsonObject object){
		super(object);
	}
	
	public void init(JsonObject object){
		super.init(object);
		setMod(object.get(MODULUS).getAsString());
		setExp(object.get(EXPONENT).getAsString());
	}

	@Override
	public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
		// TODO Auto-generated method stub
		byte[] modulusByte = Base64.decodeBase64(mod);
		BigInteger modulus = new BigInteger(modulusByte);
		byte[] exponentByte = Base64.decodeBase64(exp);
		BigInteger exponent = new BigInteger(exponentByte);
		
		RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
		KeyFactory factory = KeyFactory.getInstance("RSA");
		PublicKey pub = factory.generatePublic(spec);
		
		return pub;
	}
}