package usta.application.usecase;

import java.time.*;
import java.util.*;
import usta.application.port.in.QueryIntegration;
import usta.application.port.out.*;
import usta.domain.model.*;

public final class IntegrationQueryService implements QueryIntegration {
    private final RequestRepository requests;
    private final DocumentRepository documents;
    private final Transaction transaction;
    private final Clock clock;
    private final Duration alertAge;
    private final int pageSize;

    public IntegrationQueryService(RequestRepository requests, DocumentRepository documents, Transaction transaction,
                                   Clock clock, Duration alertAge, int pageSize) {
        if (alertAge.isNegative() || pageSize < 1) throw new IllegalArgumentException("Configuración de consulta inválida.");
        this.requests = requests;
        this.documents = documents;
        this.transaction = transaction;
        this.clock = clock;
        this.alertAge = alertAge;
        this.pageSize = pageSize;
    }

    public List<IntegrationRequest> requests() { return requests.list(); }
    public IntegrationRequest request(long id) { return requests.get(id); }
    public List<ProcessingEvent> history(long id) { return requests.history(id); }
    public List<IntegrationRequest> alerts() { return requests.needingAttention(clock.instant().minus(alertAge)); }
    public List<BillingDocument> documents() { return documents.list(); }
    public BillingDocument document(long id) { return documents.get(id); }

    public List<BillingDocument> forSysCafe(LocalDate date, int page, String actor) {
        if (date == null || page < 0) throw new IllegalArgumentException("Revise la fecha y la página.");
        return transaction.run(() -> {
            var results = documents.forDate(date, page, pageSize);
            for (var result : results) {
                requests.append(ProcessingEvent.builder().requestId(result.requestId()).actor(actor)
                        .occurredAt(clock.instant()).operation(ProcessingEvent.Operation.INCLUDED_IN_QUERY)
                        .outcome(ProcessingEvent.Outcome.SUCCESS).build());
            }
            return results.stream().map(DocumentRepository.Published::document).toList();
        });
    }
    public void deleteDocument(long id) {
        transaction.run(() -> {
            documents.delete(id);
            return null;
        });
    }
}
