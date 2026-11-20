package usta.application.usecase;

import java.time.Clock;
import java.util.*;
import lombok.RequiredArgsConstructor;
import usta.application.command.*;
import usta.application.port.in.ProcessDocuments;
import usta.application.port.out.*;
import usta.domain.model.*;
import usta.domain.service.BillingReconciliationService;
import static usta.domain.model.ProcessingEvent.Operation.*;
import static usta.domain.model.ProcessingEvent.Outcome.*;

@RequiredArgsConstructor
public final class ProcessingService implements ProcessDocuments {
    private final RequestRepository requests;
    private final DocumentRepository documents;
    private final EquivalenceRepository equivalences;
    private final PayloadStructure structure;
    private final PayloadReader reader;
    private final DocumentContract contract;
    private final BillingReconciliationService reconciliation;
    private final Transaction transaction;
    private final Clock clock;

    public Result receive(ReceiveDocument input) {
        structure.validate(input == null ? null : input.payload());
        long id = transaction.run(() -> {
            var request = IntegrationRequest.receive(input.sourceSystem(), input.sourceNumber(), input.payload(), clock.instant());
            long savedId = requests.save(request).getId();
            event(savedId, input.actor(), RECEIVED, SUCCESS, List.of());
            return savedId;
        });
        return retry(id, input.actor());
    }

    public Result retry(long id, String actor) {
        return transaction.run(() -> {
            var request = requests.getForUpdate(id);
            if (request.getStatus() != IntegrationRequest.Status.PENDING) {
                throw new IllegalStateException("La solicitud ya está cerrada.");
            }
            var mappings = equivalences.list();
            BillingDocument document;
            try {
                var input = reader.read(request.getOriginalPayload());
                document = switch (input) {
                    case PayloadReader.Ips ips -> new InvoiceConverter(mappings).convert(ips.invoice());
                    case PayloadReader.Prepared ready -> ready.document();
                };
                var errors = contract.errors(document, mappings, input instanceof PayloadReader.Ips);
                if (!errors.isEmpty()) throw new InputErrors(errors);
            } catch (IllegalArgumentException failure) {
                var errors = failure instanceof InputErrors content ? content.messages() : List.of(failure.getMessage());
                return finish(request, actor, ERROR, errors);
            }
            var inserted = documents.insertIfAbsent(document);
            if (inserted.isEmpty()) {
                request.markRejected();
                return finish(request, actor, ERROR, List.of("Ya existe el documento con esa identificación."));
            }
            request.markConverted(inserted.getAsLong());
            var balance = reconciliation.reconcile(document);
            if (balance.isWarning()) {
                return finish(request, actor, WARNING, List.of("Neto " + balance.getNet()
                        + ", pagos " + balance.getPaid() + ", diferencia " + balance.getDifference()));
            }
            return finish(request, actor, SUCCESS, List.of());
        });
    }

    private Result finish(IntegrationRequest request, String actor, ProcessingEvent.Outcome outcome, List<String> messages) {
        requests.save(request);
        event(request.getId(), actor, CONVERSION, outcome, messages);
        return new Result(request.getId(), request.getStatus(), request.getDocumentId(), messages);
    }

    private void event(long id, String actor, ProcessingEvent.Operation operation,
                       ProcessingEvent.Outcome outcome, List<String> messages) {
        requests.append(ProcessingEvent.builder().requestId(id).occurredAt(clock.instant()).actor(actor)
                .operation(operation).outcome(outcome).messages(messages).build());
    }
}
