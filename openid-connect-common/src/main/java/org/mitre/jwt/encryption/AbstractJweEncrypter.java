package org.mitre.jwt.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.google.common.primitives.Ints;

public abstract class AbstractJweEncrypter implements JweEncrypter {
	
	public MessageDigest md;
	
	public byte[] generateContentKey(byte[] cmk, int keyDataLen, byte[] type) throws NoSuchAlgorithmException {
		//HUGE DISCLAIMER: this won't work on windows machines that don't have jce unlimited security files installed.
		//without it, keys can't be over 128 bit in length, and SHA-128 doesn't work for message digest.
		
		//Use keyDataLen to determine instance
		md = MessageDigest.getInstance("SHA-" + Integer.toString(keyDataLen));
		
		long MAX_HASH_INPUTLEN = Long.MAX_VALUE;
		long UNSIGNED_INT_MAX_VALUE = 4294967395L;

		keyDataLen = keyDataLen / 8;
        byte[] key = new byte[keyDataLen];
        int hashLen = md.getDigestLength();
        int reps = keyDataLen / hashLen;
        if (reps > UNSIGNED_INT_MAX_VALUE) {
            throw new IllegalArgumentException("Key derivation failed");
        }
        int counter = 1;
        byte[] counterInBytes = Ints.toByteArray(counter);
        if ((counterInBytes.length + cmk.length + type.length) * 8 > MAX_HASH_INPUTLEN) {
            throw new IllegalArgumentException("Key derivation failed");
        }
        for (int i = 0; i <= reps; i++) {
            md.reset();
            md.update(Ints.toByteArray(i + 1));
            md.update(cmk);
            md.update(type);
            byte[] hash = md.digest();
            if (i < reps) {
                System.arraycopy(hash, 0, key, hashLen * i, hashLen);
            } else {
                System.arraycopy(hash, 0, key, hashLen * i, keyDataLen % hashLen);
            }
        }
        return key;
    
	}
	
}
