package usta.application.usecase;

import java.util.List;
import java.util.Optional;
import usta.application.port.in.AuthenticateUsers;
import usta.application.port.in.ManageUsers;
import usta.application.port.out.PasswordHasher;
import usta.application.port.out.Transaction;
import usta.application.port.out.UserRepository;
import usta.domain.model.Role;
import usta.domain.model.User;

public final class UserService implements ManageUsers, AuthenticateUsers {
    private static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository users;
    private final PasswordHasher hasher;
    private final Transaction transaction;
    // Se compara contra este hash cuando el usuario no existe, para que responder
    // "usuario inexistente" no sea más rápido que responder "contraseña incorrecta".
    private volatile String unknownUserHash;

    public UserService(UserRepository users, PasswordHasher hasher, Transaction transaction) {
        this.users = users;
        this.hasher = hasher;
        this.transaction = transaction;
    }

    // ---------- AuthenticateUsers ----------

    @Override
    public Optional<User> authenticate(String username, String password) {
        String name = User.normalize(username);
        Optional<User> found = name.isEmpty() ? Optional.empty() : users.findByUsername(name);
        String hash = found.map(User::getPasswordHash).orElseGet(this::dummyHash);
        boolean correct = password != null && hasher.matches(password, hash);
        return found.filter(user -> correct && user.isActive());
    }

    @Override
    public Optional<User> findActive(String username) {
        String name = User.normalize(username);
        return name.isEmpty() ? Optional.empty() : users.findByUsername(name).filter(User::isActive);
    }

    private String dummyHash() {
        String hash = unknownUserHash;
        if (hash == null) {
            hash = hasher.hash("contraseña-de-relleno");
            unknownUserHash = hash;
        }
        return hash;
    }

    // ---------- ManageUsers ----------

    @Override
    public User create(String username, String password, Role role) {
        requireValidPassword(password);
        String hash = hasher.hash(password);
        return transaction.run(() -> {
            users.lockChanges();
            String name = User.normalize(username);
            if (users.findByUsername(name).isPresent()) {
                throw new IllegalStateException("Ya existe un usuario con ese nombre.");
            }
            return users.save(User.builder().username(name).passwordHash(hash).role(role).active(true).build());
        });
    }

    @Override
    public User update(long id, Role role, Boolean active, String newPassword) {
        String hash = null;
        if (newPassword != null) {
            requireValidPassword(newPassword);
            hash = hasher.hash(newPassword);
        }
        String newHash = hash;
        return transaction.run(() -> {
            users.lockChanges();
            User current = users.get(id);
            User updated = current;
            if (role != null) updated = updated.withRole(role);
            if (active != null) updated = updated.withActive(active);
            if (newHash != null) updated = updated.withPasswordHash(newHash);
            if (current.isActiveAdministrator() && !updated.isActiveAdministrator()) {
                requireAnotherAdministrator();
            }
            return users.save(updated);
        });
    }

    @Override
    public User get(long id) { return users.get(id); }

    @Override
    public List<User> list() { return users.list(); }

    @Override
    public void delete(long id) {
        transaction.run(() -> {
            users.lockChanges();
            User current = users.get(id);
            if (current.isActiveAdministrator()) requireAnotherAdministrator();
            users.delete(id);
            return null;
        });
    }

    @Override
    public boolean ensureInitialAdministrator(String username, String password) {
        requireValidPassword(password);
        String hash = hasher.hash(password);
        return transaction.run(() -> {
            users.lockChanges();
            if (users.count() > 0) return false;
            users.save(User.builder().username(username).passwordHash(hash).role(Role.ADMINISTRATOR).active(true).build());
            return true;
        });
    }

    // ---------- Reglas ----------

    private void requireValidPassword(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres.");
        }
    }

    // Se llama con el bloqueo tomado: el conteo no puede cambiar hasta terminar la transacción.
    private void requireAnotherAdministrator() {
        if (users.countActiveAdministrators() <= 1) {
            throw new IllegalStateException("Debe quedar al menos un administrador activo.");
        }
    }
}
