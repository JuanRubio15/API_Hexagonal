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
public class JpaHealthBillingData implements DetailRepository<HealthBillingData> {
    @Inject JpaAccess db;
    @Inject JpaBillingDocument documents;

    public List<HealthBillingData> list() {
        return db.all(HealthBillingDataEntity.class).stream().map(EntityMapper::toDomain).toList();
    }

    public HealthBillingData get(long id) { return EntityMapper.toDomain(db.get(HealthBillingDataEntity.class, id)); }

    public HealthBillingData save(long documentId, HealthBillingData value) {
        db.lockWriters();
        var row = value.getId() == null ? new HealthBillingDataEntity() : db.get(HealthBillingDataEntity.class, value.getId());
        EntityMapper.copy(value, row);
        row.document = db.get(BillingDocumentEntity.class, documentId);
        if (row.id == null) db.em.persist(row);
        db.em.flush();
        return EntityMapper.toDomain(row);
    }
    public void delete(long id) { db.lockWriters(); db.em.remove(db.get(HealthBillingDataEntity.class,id)); }
    public BillingDocument lockOwner(long id) { db.lockWriters(); return documents.get(db.get(HealthBillingDataEntity.class,id).document.id); }
}
