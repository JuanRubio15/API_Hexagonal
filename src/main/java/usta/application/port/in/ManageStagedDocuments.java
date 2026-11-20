package usta.application.port.in;

import usta.domain.model.BillingDocument;

public interface ManageStagedDocuments {
    void replace(long id, BillingDocument document, String actor);
    void delete(long id, String actor);
}
