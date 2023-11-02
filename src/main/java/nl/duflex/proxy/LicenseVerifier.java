package nl.duflex.proxy;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;

import java.security.interfaces.RSAPublicKey;
import java.util.logging.Logger;

public class LicenseVerifier {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final JWTVerifier verifier;

    public LicenseVerifier(final LicenseVerifierPublicKey licenseVerifierPublicKey) {
        this.verifier = JWT.require(Algorithm.RSA512(licenseVerifierPublicKey.getRsaPublicKey(), null))
                .withClaimPresence("features")
                .withClaimPresence("details")
                .build();
    }

    public License verify(final String token) throws InvalidLicenseException {
        try {
            final DecodedJWT decodedJWT = this.verifier.verify(token);

            final var featuresClaim = decodedJWT.getClaim("features");
            final var detailsClaim = decodedJWT.getClaim("details");

            final var features = featuresClaim.asList(String.class);
            final var details = detailsClaim.asString();

            return new License(features, details);
        } catch (final JWTVerificationException exception) {
            throw new InvalidLicenseException("License verification failed, message: " + exception.getMessage());
        }
    }
}
