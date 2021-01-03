package Shared;

public class KeyPairB64 {
    private final String pvt;
    private final String pub;

    public KeyPairB64(String pub, String pvt) {
        this.pvt = pvt;
        this.pub = pub;
    }

    public String getPrivateKey() {
        return pvt;
    }

    public String getPublicKey() {
        return pub;
    }
}
