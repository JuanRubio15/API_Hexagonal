package usta.domain.model;

import java.util.Locale;
import java.util.regex.Pattern;

/** Usuario del sistema. Es inmutable: los cambios producen una copia (withRole, withActive...). */
public final class User {
    private static final Pattern VALID_NAME = Pattern.compile("[a-z0-9._-]{3,50}");

    private final Long id;
    private final String username;
    private final String passwordHash;
    private final Role role;
    private final boolean active;

    private User(Long id, String username, String passwordHash, Role role, boolean active) {
        String name = normalize(username);
        if (!VALID_NAME.matcher(name).matches()) {
            throw new IllegalArgumentException(
                    "El usuario debe tener entre 3 y 50 caracteres: letras, números, punto, guion o guion bajo.");
        }
        if (passwordHash == null || passwordHash.isBlank()) {
            throw new IllegalArgumentException("El usuario requiere una contraseña.");
        }
        if (role == null) {
            throw new IllegalArgumentException("El usuario requiere un rol.");
        }
        this.id = id;
        this.username = name;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    /** Los nombres de usuario no distinguen mayúsculas ni espacios en los extremos. */
    public static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }

    public boolean isActiveAdministrator() { return active && role == Role.ADMINISTRATOR; }

    public User withRole(Role newRole) { return new User(id, username, passwordHash, newRole, active); }
    public User withActive(boolean newActive) { return new User(id, username, passwordHash, role, newActive); }
    public User withPasswordHash(String newHash) { return new User(id, username, newHash, role, active); }

    public static final class Builder {
        private Long id;
        private String username;
        private String passwordHash;
        private Role role;
        private boolean active = true;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder username(String username) { this.username = username; return this; }
        public Builder passwordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
        public Builder role(Role role) { this.role = role; return this; }
        public Builder active(boolean active) { this.active = active; return this; }
        public User build() { return new User(id, username, passwordHash, role, active); }
    }
}
