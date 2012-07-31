package org.mitre.jwt.encryption.impl;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.mitre.jwe.model.Jwe;
import org.mitre.jwt.encryption.AbstractJweEncrypter;
import org.mitre.jwt.encryption.JweAlgorithms;
import org.mitre.jwt.signer.impl.HmacSigner;

public class RsaEncrypter extends AbstractJweEncrypter {
	
	private PublicKey publicKey;
	private PrivateKey privateKey;

	public Jwe encryptAndSign(Jwe jwe) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		
		String alg = jwe.getHeader().getAlgorithm();
		String integrityAlg = jwe.getHeader().getIntegrity();
		
		if(alg.equals("RSA1_5") || alg.equals("RSA-OAEP") || alg.equals("ECDH-ES") || alg.equals("A128KW") || alg.equals("A256KW")) {
			
			//generate random content master key

			//check what the key length is
			String kdf = jwe.getHeader().getKeyDerivationFunction();
			String keyLength = JweAlgorithms.getByName(kdf);
			int keyBitLength = Integer.parseInt(keyLength);
			
			byte[] contentMasterKey = new byte[keyBitLength];
			new Random().nextBytes(contentMasterKey);

			byte[] contentEncryptionKey = null;
			byte[] contentIntegrityKey = null;
			
			//generate cek and cik
			contentEncryptionKey = generateContentKey(contentMasterKey, keyBitLength, "Encryption".getBytes());
			contentIntegrityKey = generateContentKey(contentMasterKey, keyBitLength, "Integrity".getBytes());
			
			//encrypt claims and cmk to get ciphertext and encrypted key
			jwe.setCiphertext(encryptClaims(jwe, contentEncryptionKey));
			jwe.setEncryptedKey(encryptKey(jwe, contentMasterKey));
			
			//Signer must be hmac
			if(integrityAlg.equals("HS256") || integrityAlg.equals("HS384") || integrityAlg.equals("HS512")){
			
				HmacSigner hmacSigner = new HmacSigner(contentIntegrityKey); 
				jwe = (Jwe) hmacSigner.sign(jwe);
				
			} else {
				throw new IllegalArgumentException(integrityAlg + " is not a valid integrity value algorithm for signing.");
			}
			
		} else {
			throw new IllegalArgumentException(alg + " is not a valid encrypting algorithm.");
		}
		
		return jwe;
	}

	public byte[] encryptKey(Jwe jwe, byte[] contentMasterKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		
		if(jwe.getHeader().getAlgorithm().equals("RSA1_5")){
		
			Cipher cipher = Cipher.getInstance("RSA");
			cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
			byte[] encryptedKey = cipher.doFinal(contentMasterKey);
			return encryptedKey;
		
		} else {
			throw new IllegalArgumentException(jwe.getHeader().getAlgorithm() + " is not a supported algorithm");
		}
		
	}

	public byte[] encryptClaims(Jwe jwe, byte[] contentEncryptionKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeySpecException {
		
		byte[] iv = new byte[16];
		//look for iv value in header, if not there make one
		if(jwe.getHeader().getInitializationVector() != null){
			iv = Base64.decodeBase64(jwe.getHeader().getInitializationVector());
		} else {
			new Random().nextBytes(iv);
			jwe.getHeader().setIv(Base64.encodeBase64String(iv));
		}
		
		String encMethod = jwe.getHeader().getEncryptionMethod();
		//TODO: should also check for A128GCM and A256GCM, but Cipher.getInstance() does not support the GCM mode. For now, don't use them
		if(encMethod.equals("A128CBC") || encMethod.equals("A256CBC")) {
			
			String mode = JweAlgorithms.getByName(encMethod);
			
			Cipher cipher = Cipher.getInstance("AES/" + mode + "/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(contentEncryptionKey, "AES"), new IvParameterSpec(iv));
			byte[] cipherText = cipher.doFinal(jwe.getCiphertext());
			return cipherText;

		} else {
			throw new IllegalArgumentException(jwe.getHeader().getEncryptionMethod() + " is not a supported encryption method");
		}

	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(PublicKey publicKey) {
		this.publicKey = publicKey;
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
}
