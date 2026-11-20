package usta.infrastructure.security;

import jakarta.enterprise.context.ApplicationScoped;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import usta.application.port.out.PasswordHasher;

/** Hash de contraseñas con PBKDF2 (incluido en el JDK). Formato: pbkdf2$iteraciones$sal$hash (Base64). */
@ApplicationScoped
public class Pbkdf2PasswordHasher implements PasswordHasher {
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 120_000;
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;
    private final SecureRandom random = new SecureRandom();

    @Override
    public String hash(String password) {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        return "pbkdf2$" + ITERATIONS + "$" + encode(salt) + "$" + encode(derive(password, salt, ITERATIONS));
    }

    @Override
    public boolean matches(String password, String storedHash) {
        if (password == null || storedHash == null) return false;
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !parts[0].equals("pbkdf2")) return false;
        try {
            int iterations = Integer.parseInt(parts[1]);
            if (iterations <= 0) return false;
            byte[] expected = Base64.getDecoder().decode(parts[3]);
            byte[] actual = derive(password, Base64.getDecoder().decode(parts[2]), iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (IllegalArgumentException invalidFormat) {
            return false;
        }
    }

    private static byte[] derive(String password, byte[] salt, int iterations) {
        try {
            var spec = new PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance(ALGORITHM).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña.", e);
        }
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }
}
