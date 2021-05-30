package net.notfab.lindsey.core.framework;

import io.github.netvl.ecoji.Ecoji;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;

public class EncryptionUtils {

    public static String aesEcojiEncrypt(String key, String data) {
        try {
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            return Ecoji.getEncoder()
                    .readFrom(cipher.doFinal(data.getBytes()))
                    .writeToString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String aesEcojiDecrypt(String key, String data) {
        String decrypted;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            Key aesKey = new SecretKeySpec(key.getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, aesKey);
            byte[] decoded = Ecoji.getDecoder()
                    .readFrom(data)
                    .writeToBytes();
            decrypted = new String(cipher.doFinal(decoded));
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
        return decrypted;
    }

}
