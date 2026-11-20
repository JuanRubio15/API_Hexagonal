package usta.application.usecase;

import java.util.List;
import usta.application.port.in.ManageDocumentDetails;
import usta.application.port.out.*;
import usta.domain.model.*;

public final class DocumentDetailService<T> implements ManageDocumentDetails<T> {
    private final DetailRepository<T> details;
    private final Transaction transaction;
    private final Class<T> type;

    public DocumentDetailService(Class<T> type, DetailRepository<T> details, Transaction transaction) {
        this.type = type;
        this.details = details;
        this.transaction = transaction;
    }
    public List<T> list() { return details.list(); }
    public T get(long id) { return details.get(id); }
    public T save(long documentId, T detail) {
        return transaction.run(() -> {
            if (detail instanceof Item item && item.getId() != null) {
                BillingDocument current = details.lockOwner(item.getId());
                if (current.getId() != documentId) current.removeItem(item.getId());
            }
            return details.save(documentId, type.cast(detail));
        });
    }
    public void delete(long id) {
        transaction.run(() -> {
            BillingDocument document = details.lockOwner(id);
            if (type == Item.class) document.removeItem(id);
            details.delete(id);
            return null;
        });
    }
}
