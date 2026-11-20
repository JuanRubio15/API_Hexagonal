package usta.application.port.out;

public interface PasswordHasher {
    /** Devuelve el hash con su sal e iteraciones incluidos, listo para guardar. */
    String hash(String password);

    boolean matches(String password, String storedHash);
}
