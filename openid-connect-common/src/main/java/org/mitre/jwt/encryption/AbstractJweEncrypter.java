package org.mitre.jwt.encryption;

import java.security.MessageDigest;

public abstract class AbstractJweEncrypter implements JwtEncrypter {
	
	public MessageDigest md;
	
	public byte[] generateContentKey(byte[] cmk, int keyDataLen, byte[] type) {

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
	
    public byte[] intToFourBytes(int i) {
        byte[] res = new byte[4];
        res[0] = (byte) (i >>> 24);
        res[1] = (byte) ((i >>> 16) & 0xFF);
        res[2] = (byte) ((i >>> 8) & 0xFF);
        res[3] = (byte) (i & 0xFF);
        return res;
    }


}
