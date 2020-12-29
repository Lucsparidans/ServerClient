package Server;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class Encryption {

    public static String encrypt(String stringPubKey, String text){

        try {
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(stringPubKey.getBytes()));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);

            return Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));
        }catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e){
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String stringPrvKey, String text) {

        try {
            byte[] data = Base64.getDecoder().decode(stringPrvKey.getBytes());

            java.security.Security.addProvider(
                    new org.bouncycastle.jce.provider.BouncyCastleProvider()
            );

            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);

            byte[] data2 = Base64.getDecoder().decode(text.getBytes());

            byte[] ciphertext = cipher.doFinal(data2);

            return new String(ciphertext);
        } catch(IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException e){
            e.printStackTrace();
        }
        return null;
    }

}
