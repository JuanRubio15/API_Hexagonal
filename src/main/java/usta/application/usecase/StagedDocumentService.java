package usta.application.usecase;

import java.time.Clock;
import java.util.List;
import usta.application.port.in.ManageStagedDocuments;
import usta.application.port.out.*;
import usta.domain.model.*;
import usta.domain.service.BillingReconciliationService;

public record StagedDocumentService(StagedDocumentStore store, RequestRepository requests,
        DocumentContract contract, EquivalenceRepository mappings, Transaction transaction,
        Clock clock) implements ManageStagedDocuments {

    public void replace(long id, BillingDocument document, String actor) {
        transaction.run(() -> {
            var requestId = store.lockRequestForDocument(id);
            var issues = contract.errors(document, mappings.list(), false);
            if (!issues.isEmpty()) throw new IllegalArgumentException(String.join("; ", issues));
            store.replace(id, document);
            requestId.ifPresent(linkedId -> {
                var balance = new BillingReconciliationService().reconcile(document);
                var info = "Documento " + id + (balance.isWarning() ? ", diferencia con pagos: " + balance.getDifference() : "");
                recordEvent(linkedId, actor, ProcessingEvent.Operation.DOCUMENT_UPDATED, balance.isWarning(), info);
            });
            return null;
        });
    }

    public void delete(long id, String actor) {
        transaction.run(() -> {
            var requestId = store.lockRequestForDocument(id);
            requestId.ifPresent(linkedId -> {
                var request = requests.getForUpdate(linkedId);
                request.markDocumentDeleted();
                requests.save(request);
                recordEvent(linkedId, actor, ProcessingEvent.Operation.DOCUMENT_DELETED, false, "Documento " + id);
            });
            store.delete(id);
            return null;
        });
    }

    private void recordEvent(long requestId, String actor, ProcessingEvent.Operation operation, boolean warning, String message) {
        requests.append(ProcessingEvent.builder().requestId(requestId).actor(actor).occurredAt(clock.instant())
                .operation(operation).outcome(warning ? ProcessingEvent.Outcome.WARNING : ProcessingEvent.Outcome.SUCCESS)
                .messages(List.of(message)).build());
    }
}
