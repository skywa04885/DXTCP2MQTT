package nl.duflex.proxy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class LicenseVerifierPublicKeyFactory {
    public static LicenseVerifierPublicKey createFromResource(final String resource) {
        try (final var publicKeyFile = ClassLoader.getSystemClassLoader().getResourceAsStream(resource)) {
            if (publicKeyFile == null) throw new Error("Public key resource is missing");

            final var publicKeyString = new String(publicKeyFile.readAllBytes(), StandardCharsets.UTF_8)
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replaceAll(System.lineSeparator(), "")
                    .replace("-----END PUBLIC KEY-----", "");

            final var publicKeyBytes = Base64.getDecoder().decode(publicKeyString);
            final var publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            final var keyFactory = KeyFactory.getInstance("RSA");
            final var publicKey = keyFactory.generatePublic(publicKeySpec);

            return new LicenseVerifierPublicKey((RSAPublicKey) publicKey);
        } catch (final IOException | NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new Error("Failed to load public key for license validation, message: " + exception.getMessage());
        }
    }
}
