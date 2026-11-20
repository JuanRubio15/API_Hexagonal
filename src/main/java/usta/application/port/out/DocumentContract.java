package usta.application.port.out;

import java.util.List;
import usta.domain.model.*;

/** Comprueba el contrato SysCafé 1.3 y los códigos de destino, incluidos los pares de pago y ajuste.
 * La entrada IPS conserva sus restricciones locales. Devuelve los problemas de formato agrupados. */
public interface DocumentContract {
    List<String> errors(BillingDocument document, List<Equivalence> mappings, boolean fromIps);
}
