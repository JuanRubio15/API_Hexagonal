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
public class JpaItem implements DetailRepository<Item> {
    @Inject JpaAccess db;
    @Inject JpaBillingDocument documents;

    public List<Item> list() {
        return db.all(ItemEntity.class).stream().map(EntityMapper::toDomain).toList();
    }

    public Item get(long id) { return EntityMapper.toDomain(db.get(ItemEntity.class, id)); }

    public Item save(long documentId, Item value) {
        db.lockWriters();
        var row = value.getId() == null ? new ItemEntity() : db.get(ItemEntity.class, value.getId());
        EntityMapper.copy(value, row);
        row.document = db.get(BillingDocumentEntity.class, documentId);
        if (row.id == null) db.em.persist(row);
        db.em.flush();
        return EntityMapper.toDomain(row);
    }
    public void delete(long id) { db.lockWriters(); db.em.remove(db.get(ItemEntity.class,id)); }
    public BillingDocument lockOwner(long id) { db.lockWriters(); return documents.get(db.get(ItemEntity.class,id).document.id); }
}
