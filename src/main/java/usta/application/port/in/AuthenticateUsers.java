package usta.application.port.in;

import java.util.Optional;
import usta.domain.model.User;

public interface AuthenticateUsers {
    /** Devuelve el usuario si el nombre y la contraseña son correctos y la cuenta está activa. */
    Optional<User> authenticate(String username, String password);

    /** Busca un usuario activo por nombre (para validar un token ya emitido). */
    Optional<User> findActive(String username);
}
