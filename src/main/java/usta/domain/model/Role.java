package usta.domain.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

/** Roles del sistema y lo que puede hacer cada uno. Para cambiar un permiso se edita solo este enum. */
public enum Role {
    ADMINISTRATOR(Permission.READ, Permission.CONVERT, Permission.MANAGE),
    OPERATOR(Permission.READ, Permission.CONVERT),
    VIEWER(Permission.READ);

    private final Set<Permission> permissions;

    Role(Permission... permissions) {
        this.permissions = EnumSet.copyOf(Arrays.asList(permissions));
    }

    public boolean can(Permission permission) {
        return permissions.contains(permission);
    }

    /** Acepta el nombre en inglés o en español (ADMINISTRADOR, OPERADOR, VISUALIZADOR). */
    public static Role parse(String text) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Indique el rol: ADMINISTRADOR, OPERADOR o VISUALIZADOR.");
        }
        return switch (text.trim().toUpperCase(Locale.ROOT)) {
            case "ADMINISTRATOR", "ADMINISTRADOR" -> ADMINISTRATOR;
            case "OPERATOR", "OPERADOR" -> OPERATOR;
            case "VIEWER", "VISUALIZADOR" -> VIEWER;
            default -> throw new IllegalArgumentException(
                    "Rol desconocido: " + text + ". Use ADMINISTRADOR, OPERADOR o VISUALIZADOR.");
        };
    }
}
