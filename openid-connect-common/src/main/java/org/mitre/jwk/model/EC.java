package org.mitre.jwk.model;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.ECFieldF2m;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.JCEECPublicKey;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;

import com.google.gson.JsonObject;

public class EC extends AbstractJwk{
	
	public static final String CURVE = "crv";
	public static final String X = "x";
	public static final String Y = "y";
	
	private String crv;
	private String x;
	private String y;
	
	JsonObject object = new JsonObject();
	
	public String getCrv() {
		return crv;
	}

	public void setCrv(String crv) {
		this.crv = crv;
	}

	public String getX() {
		return x;
	}

	public void setX(String x) {
		this.x = x;
	}

	public String getY() {
		return y;
	}

	public void setY(String y) {
		this.y = y;
	}

	public EC(JsonObject object) {
		super(object);
	}
	
	public void init(JsonObject object){
		super.init(object);
		setCrv(object.get(CURVE).getAsString());
		setX(object.get(X).getAsString());
		setY(object.get(Y).getAsString());
	}

	@Override
	public PublicKey getKey() throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
		// TODO Auto-generated method stub
		
		byte[] x_byte = Base64.decodeBase64(x);
		BigInteger x_int = new BigInteger(x_byte);
		byte[] y_byte = Base64.decodeBase64(y);
		BigInteger y_int = new BigInteger(y_byte);

		ECNamedCurveParameterSpec curveSpec = ECNamedCurveTable.getParameterSpec(crv);
		BigInteger orderOfGen = curveSpec.getH();
		int cofactor = Math.abs(curveSpec.getN().intValue());
		ECCurve crv = curveSpec.getCurve();
		BigInteger a = crv.getA().toBigInteger();
		BigInteger b = crv.getB().toBigInteger();
		int fieldSize = crv.getFieldSize();
		ECFieldF2m field = new ECFieldF2m(fieldSize);
		EllipticCurve curve = new EllipticCurve(field, a, b);
		//ECPoint.Fp point = new ECPoint.Fp(curve, arg1, arg2);
		return null;
		
		//ECParameterSpec paramSpec = new ECParameterSpec(curve, point, orderOfGen, cofactor);
		//ECPublicKeySpec spec = new ECPublicKeySpec(point, paramSpec);
		//PublicKey key = new JCEECPublicKey("ECDCA", spec);
		
		//return key;
	}
}