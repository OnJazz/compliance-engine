package com.jasonvennin.compliance.infrastructure.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
public class RsaKeyConfiguration {

    @Bean
    public JwtEncoder jwtEncoder(
            RSAPublicKey publicKey,
            RSAPrivateKey privateKey
    ) {
        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        return new NimbusJwtEncoder(
                new ImmutableJWKSet<>(new JWKSet(jwk))
        );
    }

    @Bean
    public RSAPublicKey rsaPublicKey(
            @Value("${jwt.public-key}") Resource publicKeyResource
    ) {
        return loadPublicKey(publicKeyResource);
    }

    @Bean
    public RSAPrivateKey rsaPrivateKey(
            @Value("${jwt.private-key}") Resource privateKeyResource
    ) {
        return loadPrivateKey(privateKeyResource);
    }

    private RSAPublicKey loadPublicKey(Resource resource) {
        try {
            String key = readKey(resource);

            byte[] decoded = Base64.getDecoder().decode(
                    key
                            .replace("-----BEGIN PUBLIC KEY-----", "")
                            .replace("-----END PUBLIC KEY-----", "")
                            .replaceAll("\\s", "")
            );

            X509EncodedKeySpec keySpec =
                    new X509EncodedKeySpec(decoded);

            return (RSAPublicKey) KeyFactory
                    .getInstance("RSA")
                    .generatePublic(keySpec);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load RSA public key",
                    exception
            );
        }
    }

    private RSAPrivateKey loadPrivateKey(Resource resource) {
        try {
            String key = readKey(resource);

            byte[] decoded = Base64.getDecoder().decode(
                    key
                            .replace("-----BEGIN PRIVATE KEY-----", "")
                            .replace("-----END PRIVATE KEY-----", "")
                            .replaceAll("\\s", "")
            );

            PKCS8EncodedKeySpec keySpec =
                    new PKCS8EncodedKeySpec(decoded);

            return (RSAPrivateKey) KeyFactory
                    .getInstance("RSA")
                    .generatePrivate(keySpec);

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load RSA private key",
                    exception
            );
        }
    }

    private String readKey(Resource resource) throws IOException {
        return resource.getContentAsString(StandardCharsets.UTF_8);
    }
}