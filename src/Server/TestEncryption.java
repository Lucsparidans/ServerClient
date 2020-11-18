package Server;

import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TestEncryption {

    public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidKeySpecException, IOException {
        String plainText = "Fuck you";

        String stringPubKey = "MIICIjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEA33bh0ZPszl9wxWqP5TWtvO44QrqSdNA3yoPCRf8gBXvthOK9QK/xLART+q9yPIXjdUyR855GeAiPXvbee7IjzgY1ZGKBfpTxojPn13M+xRCae0SCezTWGZ1sxYYB1FRUsfQaTzzPC6wgJrYwh4BoRruWTruXzq3UbtQxmDKqCO2D0nIzrnubZQLfXBFMyGkVnqghiGblbXd7TcT6eJA7kGLnrCWhgt/TlLQwudOZ1VdfB7cHcNX8gCHV4E9rEoPMTEoc+kzXNEyWkdnivuNg7z1sGW2jDuHCcOEYJxwq6UaRn3qwe54VfkkMonR+d5UYuwJIbWuUhog5jcUbCQ5v8YhThk3vgiE6sDulAx1cOtCBk1JofTTnNOxzLOnxz82UUBYB0hUXRsWl8U15wELXIAw5glUzc0gVLMJeiLKwye7zCebpEL+HhKtTBcW6q7VWV4cu3dls18Tf+UjtMB+wRvh25y0mBNK+odKmVmko2Lf+IaAsbYvcjQTqxCVvIGqvQ9683RFBu1cPQkyiy60KldkRWVjTei98PjQafcqhxTAgUCBByoNuzTn+w0Mi1By4kIWkqOXEQWUQ0aprHPsk7v//aIJM2rBltcGk0EedwWvoiGaKzjdqIkXEP7RDM/h2V6VpYYAuPxsnSx1yPWfnixcoefQDDWXvBcvuvuRtAmcCAwEAAQ==";
        String stringPrvKey = "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";

        String cText = encrypt(stringPubKey, plainText);

        System.out.println(cText);

        String pText = decrypt(stringPrvKey, cText);

        System.out.println(pText);


    }

    public static String encrypt(String stringPubKey, String text) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.getDecoder().decode(stringPubKey.getBytes()));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        String encryptedString = Base64.getEncoder().encodeToString(cipher.doFinal(text.getBytes()));

        return encryptedString;
    }

    public static String decrypt(String stringPrvKey, String text) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {

        byte[] data = Base64.getDecoder().decode(stringPrvKey.getBytes());

        java.security.Security.addProvider(
                new org.bouncycastle.jce.provider.BouncyCastleProvider()
        );

        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(data);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte [] data2 = Base64.getDecoder().decode(text.getBytes());

        byte[] ciphertext = cipher.doFinal(data2);

        String decryptedString = new String(ciphertext);

        return decryptedString;
    }

}
