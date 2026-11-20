package usta.infrastructure.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import usta.application.port.in.AuthenticateUsers;
import usta.domain.model.User;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;

@ApplicationScoped
public class Credentials {
    @Inject AuthenticateUsers users;
    @ConfigProperty(name="billing.jwt-secret") Optional<String> secret;
    @ConfigProperty(name="billing.syscafe-token") Optional<String> consumerToken;

    private boolean matches(Optional<String> expected,String supplied) {
        return expected.filter(s -> !s.isBlank()).isPresent() && supplied != null && MessageDigest.isEqual(
                expected.get().getBytes(StandardCharsets.UTF_8),supplied.getBytes(StandardCharsets.UTF_8));
    }
    /** Usuario y contraseña contra la tabla users. */
    public Optional<User> login(String name,String pass) { return users.authenticate(name,pass); }
    public boolean consumer(String bearer) { return matches(consumerToken,bearer); }
    private Algorithm algorithm() {
        return Algorithm.HMAC256(secret.filter(s -> s.length()>=32).orElseThrow(
                () -> new IllegalStateException("Configure BILLING_JWT_SECRET de al menos 32 caracteres.")));
    }
    public String issue(User user) {
        return JWT.create().withIssuer("ips-billing").withSubject(user.getUsername()).withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(3600)).sign(algorithm());
    }
    /** Verifica el token y vuelve a leer al usuario: si se desactiva o cambia de rol, el token deja de servir o se actualiza. */
    public Optional<User> fromToken(String token) {
        if (secret.filter(s -> s.length()>=32).isEmpty()) return Optional.empty();
        try {
            var subject = JWT.require(algorithm()).withIssuer("ips-billing").build().verify(token).getSubject();
            return users.findActive(subject);
        } catch (JWTVerificationException invalid) { return Optional.empty(); }
    }
}
