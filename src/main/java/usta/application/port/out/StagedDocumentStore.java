package usta.application.port.out;

import usta.domain.model.BillingDocument;
import java.util.OptionalLong;

public interface StagedDocumentStore {
    OptionalLong lockRequestForDocument(long id);
    void replace(long id, BillingDocument document);
    void delete(long id);
}
