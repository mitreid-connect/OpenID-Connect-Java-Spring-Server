package org.mitre.jwt.encryption;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        byte[] counterInBytes = intToFourBytes(counter);
        if ((counterInBytes.length + cmk.length + type.length) * 8 > MAX_HASH_INPUTLEN) {
            throw new IllegalArgumentException("Key derivation failed");
        }
        for (int i = 0; i <= reps; i++) {
            md.reset();
            md.update(intToFourBytes(i + 1));
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
	
	// this is a utility function, shouldn't be in the public interface for this class
    protected byte[] intToFourBytes(int i) {
        byte[] res = new byte[4];
        res[0] = (byte) (i >>> 24);
        res[1] = (byte) ((i >>> 16) & 0xFF);
        res[2] = (byte) ((i >>> 8) & 0xFF);
        res[3] = (byte) (i & 0xFF);
        return res;
    }

}
