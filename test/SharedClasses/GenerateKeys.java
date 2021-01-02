package SharedClasses;


import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class GenerateKeys {
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair keyPair = kpg.generateKeyPair();
        Base64.Encoder encoder = Base64.getEncoder();
        System.out.println(encoder.encodeToString(keyPair.getPublic().getEncoded()));
        System.out.println(encoder.encodeToString(keyPair.getPrivate().getEncoded()));
    }
}
