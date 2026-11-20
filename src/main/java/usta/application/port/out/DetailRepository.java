package usta.application.port.out;

import java.util.List;
import usta.domain.model.BillingDocument;

/** Un repositorio tipado por clase de detalle. Todos los escritores bloquean el mismo documento padre. */
public interface DetailRepository<T> {
    List<T> list();
    T get(long id);
    BillingDocument lockOwner(long detailId);
    /** Admite traslado al editar. Bloquea el destino y verifica que exista. El ID ausente indica creación. */
    T save(long documentId, T detail);
    void delete(long detailId);
}
