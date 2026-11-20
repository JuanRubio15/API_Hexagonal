package usta.infrastructure.adapter.out.persistence.adapter;

import usta.domain.model.*;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.*;
import usta.application.port.out.*;
import usta.infrastructure.adapter.out.persistence.entity.*;
import usta.infrastructure.adapter.out.persistence.mapper.EntityMapper;
@ApplicationScoped
@Transactional
public class JpaPayment implements DetailRepository<Payment> {
    @Inject JpaAccess db;
    @Inject JpaBillingDocument documents;

    public List<Payment> list() {
        return db.all(PaymentEntity.class).stream().map(EntityMapper::toDomain).toList();
    }

    public Payment get(long id) { return EntityMapper.toDomain(db.get(PaymentEntity.class, id)); }

    public Payment save(long documentId, Payment value) {
        db.lockWriters();
        var row = value.getId() == null ? new PaymentEntity() : db.get(PaymentEntity.class, value.getId());
        EntityMapper.copy(value, row);
        row.document = db.get(BillingDocumentEntity.class, documentId);
        if (row.id == null) db.em.persist(row);
        db.em.flush();
        return EntityMapper.toDomain(row);
    }
    public void delete(long id) { db.lockWriters(); db.em.remove(db.get(PaymentEntity.class,id)); }
    public BillingDocument lockOwner(long id) { db.lockWriters(); return documents.get(db.get(PaymentEntity.class,id).document.id); }
}
