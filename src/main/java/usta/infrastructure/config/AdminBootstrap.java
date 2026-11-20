package usta.infrastructure.config;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.util.Optional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import usta.application.port.in.ManageUsers;

/** Si la tabla users está vacía, crea el primer administrador con billing.user y billing.password. */
@ApplicationScoped
public class AdminBootstrap {
    @Inject ManageUsers users;
    @ConfigProperty(name="billing.user") Optional<String> user;
    @ConfigProperty(name="billing.password") Optional<String> password;

    void onStart(@Observes StartupEvent event) {
        if (user.filter(s -> !s.isBlank()).isEmpty() || password.filter(s -> !s.isBlank()).isEmpty()) return;
        try {
            if (users.ensureInitialAdministrator(user.get(), password.get())) {
                Log.info("Se creó el administrador inicial '" + user.get() + "'. Cambie su contraseña desde /usuarios/{id}.");
            }
        } catch (IllegalArgumentException invalid) {
            Log.warn("No se creó el administrador inicial: " + invalid.getMessage());
        }
    }
}
