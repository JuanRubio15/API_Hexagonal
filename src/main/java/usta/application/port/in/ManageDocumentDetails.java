package usta.application.port.in;

import java.util.List;

/** Se enlaza por separado para Item, Payment, Adjustment y HealthBillingData. */
public interface ManageDocumentDetails<T> {
    List<T> list();
    T get(long id);
    T save(long documentId, T detail);
    void delete(long id);
}
