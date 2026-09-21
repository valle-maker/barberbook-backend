package com.udea.barberbook.barberbook_backend.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

// Loads the dev RSA key pair (see src/main/resources/keys, generated with openssl,
// gitignored — never commit real keys). HU-02 requires RS256-signed JWTs.
@Component
public class JwtKeyProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtKeyProvider(
        @Value("${jwt.private-key-path}") Resource privateKeyResource,
        @Value("${jwt.public-key-path}") Resource publicKeyResource
    ) {
        try {
            this.privateKey = readPrivateKey(privateKeyResource);
            this.publicKey = readPublicKey(publicKeyResource);
        } catch (Exception e) {
            throw new IllegalStateException(
                "No se pudieron cargar las llaves JWT. Genera el par con openssl en src/main/resources/keys "
                    + "(ver README).", e);
        }
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    private RSAPrivateKey readPrivateKey(Resource resource) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(stripPem(resource, "PRIVATE KEY"));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private RSAPublicKey readPublicKey(Resource resource) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(stripPem(resource, "PUBLIC KEY"));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    private String stripPem(Resource resource, String label) throws IOException {
        String content = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        return content
            .replace("-----BEGIN " + label + "-----", "")
            .replace("-----END " + label + "-----", "")
            .replaceAll("\\s", "");
    }
}
