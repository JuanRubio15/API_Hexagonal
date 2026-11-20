package usta.application.port.out;

import java.util.List;
import java.util.Optional;
import usta.domain.model.User;

public interface UserRepository {
    /** El nombre ya viene normalizado (minúsculas, sin espacios en los extremos). */
    Optional<User> findByUsername(String username);
    User get(long id);
    List<User> list();
    User save(User user);
    void delete(long id);
    long count();
    long countActiveAdministrators();
    /** Serializa los cambios de usuarios hasta que termine la transacción actual. */
    void lockChanges();
}
