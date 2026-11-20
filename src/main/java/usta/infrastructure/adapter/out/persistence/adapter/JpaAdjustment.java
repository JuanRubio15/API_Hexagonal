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
public class JpaAdjustment implements DetailRepository<Adjustment> {
    @Inject JpaAccess db;
    @Inject JpaBillingDocument documents;

    public List<Adjustment> list() {
        return db.all(AdjustmentEntity.class).stream().map(EntityMapper::toDomain).toList();
    }

    public Adjustment get(long id) { return EntityMapper.toDomain(db.get(AdjustmentEntity.class, id)); }

    public Adjustment save(long documentId, Adjustment value) {
        db.lockWriters();
        var row = value.getId() == null ? new AdjustmentEntity() : db.get(AdjustmentEntity.class, value.getId());
        EntityMapper.copy(value, row);
        row.document = db.get(BillingDocumentEntity.class, documentId);
        if (row.id == null) db.em.persist(row);
        db.em.flush();
        return EntityMapper.toDomain(row);
    }
    public void delete(long id) { db.lockWriters(); db.em.remove(db.get(AdjustmentEntity.class,id)); }
    public BillingDocument lockOwner(long id) { db.lockWriters(); return documents.get(db.get(AdjustmentEntity.class,id).document.id); }
}
