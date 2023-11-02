package nl.duflex.proxy;

import java.security.interfaces.RSAPublicKey;

public class LicenseVerifierPublicKey {
    private final RSAPublicKey rsaPublicKey;

    public LicenseVerifierPublicKey(final RSAPublicKey rsaPublicKey) {
        this.rsaPublicKey = rsaPublicKey;
    }

    public RSAPublicKey getRsaPublicKey() {
        return rsaPublicKey;
    }
}
