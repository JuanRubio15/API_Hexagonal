package usta.application.port.out;

import usta.domain.model.BillingDocument;
import java.time.LocalDate;
import java.util.*;

public interface DocumentRepository {
    /** Inserción atómica con identidad de negocio única. Vacío indica duplicado.
     * Guarda también el vínculo al cliente y los datos históricos de facturación. */
    OptionalLong insertIfAbsent(BillingDocument document);
    BillingDocument get(long id);
    List<BillingDocument> list();
    /** Orden estable por ID, página 0 sin paginación. */
    List<Published> forDate(LocalDate date, int page, int size);

    void delete(long id);

    record Published(Long requestId, BillingDocument document) {
        public Published(long requestId, BillingDocument document) { this(Long.valueOf(requestId),document); }
    }
}
