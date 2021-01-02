package Shared;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public abstract class KeyPairGenerator {
    public static KeyPairB64 generateRSAKeyPair() {
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair keyPair = kpg.generateKeyPair();
            Base64.Encoder encoder = Base64.getEncoder();
            return new KeyPairB64(encoder.encodeToString(keyPair.getPublic().getEncoded()),encoder.encodeToString(keyPair.getPrivate().getEncoded()));
        }catch (NoSuchAlgorithmException e){
            e.printStackTrace();
        }
        return null;
    }
}
