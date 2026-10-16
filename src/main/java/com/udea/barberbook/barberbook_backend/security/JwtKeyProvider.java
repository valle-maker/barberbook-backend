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

// Loads the RSA key pair that signs and verifies the JWTs (RS256, HU-02). Keys are never
// committed. Each one comes from, in this order:
//   1. JWT_PRIVATE_KEY / JWT_PUBLIC_KEY: the PEM text, or the PEM file encoded in base64
//      (a single line, convenient for hosts that only offer environment variables), or
//   2. the file at jwt.private-key-path / jwt.public-key-path (local dev, secret files).
@Component
public class JwtKeyProvider {

    private final RSAPrivateKey privateKey;
    private final RSAPublicKey publicKey;

    public JwtKeyProvider(
        @Value("${jwt.private-key-path}") Resource privateKeyResource,
        @Value("${jwt.public-key-path}") Resource publicKeyResource,
        @Value("${jwt.private-key:}") String privateKeyInline,
        @Value("${jwt.public-key:}") String publicKeyInline
    ) {
        try {
            this.privateKey = readPrivateKey(pemOf(privateKeyInline, privateKeyResource));
            this.publicKey = readPublicKey(pemOf(publicKeyInline, publicKeyResource));
        } catch (Exception e) {
            throw new IllegalStateException(
                "No se pudieron cargar las llaves JWT. Define JWT_PRIVATE_KEY y JWT_PUBLIC_KEY, o genera el "
                    + "par con openssl en src/main/resources/keys (ver README).", e);
        }
    }

    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }

    private String pemOf(String inline, Resource resource) throws IOException {
        if (inline == null || inline.isBlank()) {
            return new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        }
        String text = inline.trim();
        if (text.contains("-----BEGIN")) {
            return text.replace("\\n", "\n");
        }
        return new String(Base64.getDecoder().decode(text.replaceAll("\\s", "")), StandardCharsets.UTF_8);
    }

    private RSAPrivateKey readPrivateKey(String pem) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(stripPem(pem, "PRIVATE KEY"));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private RSAPublicKey readPublicKey(String pem) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(stripPem(pem, "PUBLIC KEY"));
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) keyFactory.generatePublic(new X509EncodedKeySpec(decoded));
    }

    private String stripPem(String content, String label) {
        return content
            .replace("-----BEGIN " + label + "-----", "")
            .replace("-----END " + label + "-----", "")
            .replaceAll("\\s", "");
    }
}
