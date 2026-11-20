package usta.infrastructure.security;

import java.security.Principal;
import usta.domain.model.Permission;
import usta.domain.model.Role;
import usta.domain.model.User;

/** Quien hace la petición. El consumidor externo (SysCafé) no tiene rol: solo puede llamar a GetFacturas. */
public final class AuthenticatedUser implements Principal {
    private final String name;
    private final Role role;

    private AuthenticatedUser(String name, Role role) {
        this.name = name;
        this.role = role;
    }

    public static AuthenticatedUser of(User user) { return new AuthenticatedUser(user.getUsername(), user.getRole()); }
    public static AuthenticatedUser consumer(String name) { return new AuthenticatedUser(name, null); }

    @Override public String getName() { return name; }
    public boolean isConsumer() { return role == null; }
    public boolean can(Permission permission) { return role != null && role.can(permission); }
    public boolean isInRole(String roleName) { return role != null && role.name().equals(roleName); }
}
