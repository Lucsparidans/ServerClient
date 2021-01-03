package SharedClasses;

import Server.Encryption;
import Shared.KeyPairB64;
import Shared.KeyPairGenerator;

public class KeyGenerationTest {
    public static void main(String[] args) {
        KeyPairB64 keyPairB64 = KeyPairGenerator.generateRSAKeyPair();
        String text = "Hey";
        System.out.println(Encryption.encrypt(keyPairB64.getPublicKey(),text));


    }
}
