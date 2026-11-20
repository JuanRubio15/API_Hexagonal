package usta.application.port.in;

import java.util.List;
import usta.domain.model.Role;
import usta.domain.model.User;

public interface ManageUsers {
    User create(String username, String password, Role role);
    /** Los parámetros nulos significan "no cambiar". */
    User update(long id, Role role, Boolean active, String newPassword);
    User get(long id);
    List<User> list();
    void delete(long id);
    /** Crea el primer administrador solo si todavía no hay usuarios. Devuelve true si lo creó. */
    boolean ensureInitialAdministrator(String username, String password);
}
