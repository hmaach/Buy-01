package com.buy01.common.security.jwt;

import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Parses RSA keys given as base64-encoded DER, the format written by scripts/generate-keys.sh.
 */
public final class RsaKeys {

    private RsaKeys() {
    }

    public static PublicKey publicKey(String base64Der) {
        try {
            byte[] der = Base64.getDecoder().decode(requireText(base64Der, "public"));
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("security.jwt.public-key is not a valid base64 X.509 RSA key", e);
        }
    }

    public static PrivateKey privateKey(String base64Der) {
        try {
            byte[] der = Base64.getDecoder().decode(requireText(base64Der, "private"));
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(der));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("security.jwt.private-key is not a valid base64 PKCS#8 RSA key", e);
        }
    }

    private static String requireText(String value, String kind) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("security.jwt." + kind + "-key is empty");
        }
        return value.strip();
    }
}
