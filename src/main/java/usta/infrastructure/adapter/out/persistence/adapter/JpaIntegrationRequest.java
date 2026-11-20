package usta.infrastructure.adapter.out.persistence.adapter;

import jakarta.persistence.*;
import java.time.*;

import usta.domain.model.*;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.*;
import usta.application.port.out.*;
import usta.infrastructure.adapter.out.persistence.entity.*;

@ApplicationScoped
@Transactional
public class JpaIntegrationRequest implements RequestRepository {
    @Inject JpaAccess db;

    private IntegrationRequest model(IntegrationRequestEntity r) {
        return IntegrationRequest.restore(r.id,r.sourceSystem,r.sourceDocumentNumber,r.originalPayload,
                r.receivedAt,IntegrationRequest.Status.valueOf(r.status),r.document == null ? null : r.document.id);
    }
    public IntegrationRequest save(IntegrationRequest value) {
        var r = value.getId() == null ? new IntegrationRequestEntity() : db.get(IntegrationRequestEntity.class,value.getId());
        r.sourceSystem = value.getSourceSystem();
        r.sourceDocumentNumber = value.getSourceDocumentNumber();
        r.originalPayload = value.getOriginalPayload();
        r.receivedAt = value.getReceivedAt();
        r.status = value.getStatus().name();
        r.document = value.getDocumentId() == null ? null : db.get(BillingDocumentEntity.class,value.getDocumentId());
        if (r.id == null) db.em.persist(r);
        return model(r);
    }
    public IntegrationRequest getForUpdate(long id) {
        var r = db.em.find(IntegrationRequestEntity.class,id,LockModeType.PESSIMISTIC_WRITE);
        if (r == null) throw new NoSuchElementException("Solicitud inexistente.");
        return model(r);
    }
    public IntegrationRequest get(long id) { return model(db.get(IntegrationRequestEntity.class,id)); }
    public List<IntegrationRequest> list() { return db.all(IntegrationRequestEntity.class).stream().map(this::model).toList(); }
    public void append(ProcessingEvent event) {
        var row = new ProcessingEventEntity();
        row.request = db.get(IntegrationRequestEntity.class,event.getRequestId());
        row.operation = event.getOperation().name();
        row.outcome = event.getOutcome().name();
        row.occurredAt = event.getOccurredAt();
        row.actor = event.getActor();
        row.messages.addAll(event.getMessages());
        db.em.persist(row);
    }
    public List<ProcessingEvent> history(long id) {
        db.get(IntegrationRequestEntity.class,id);
        return db.em.createQuery("from ProcessingEventEntity e where e.request.id=:id order by e.id",ProcessingEventEntity.class)
                .setParameter("id",id).getResultList().stream().map(e -> ProcessingEvent.builder()
                    .id(e.id).requestId(id).occurredAt(e.occurredAt).actor(e.actor).messages(e.messages)
                    .operation(ProcessingEvent.Operation.valueOf(e.operation))
                    .outcome(ProcessingEvent.Outcome.valueOf(e.outcome)).build()).toList();
    }
    public List<IntegrationRequest> needingAttention(Instant cutoff) {
        return db.em.createQuery("""
                from IntegrationRequestEntity r where r.status='REJECTED' or
                (r.status='PENDING' and (r.receivedAt <= :cutoff or exists
                (select e.id from ProcessingEventEntity e where e.request=r and e.operation='CONVERSION' and e.outcome='ERROR')))
                order by r.receivedAt,r.id
                """,IntegrationRequestEntity.class).setParameter("cutoff",cutoff).getResultList().stream().map(this::model).toList();
    }
}
