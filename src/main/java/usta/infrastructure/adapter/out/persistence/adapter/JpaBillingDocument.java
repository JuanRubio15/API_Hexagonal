package usta.infrastructure.adapter.out.persistence.adapter;
import jakarta.persistence.*;
import java.time.*;
import java.math.BigDecimal;
import usta.domain.model.*;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.*;
import usta.application.port.out.*;
import usta.infrastructure.adapter.out.persistence.adapter.JpaAccess;
import usta.infrastructure.adapter.out.persistence.entity.*;
import usta.infrastructure.adapter.out.persistence.mapper.EntityMapper;

@ApplicationScoped
@Transactional
public class JpaBillingDocument implements DocumentRepository, StagedDocumentStore {
    @Inject
    JpaAccess db;

    public OptionalLong insertIfAbsent(BillingDocument document) {
        db.lockWriters();
        boolean numbered = document.getDocumentNumber() != null && !document.getDocumentNumber().isBlank();
        String predicate = numbered ? "trim(d.documentNumber)=:number" :
                "(d.documentNumber is null or trim(d.documentNumber)='') and trim(d.externalNumber)=:number";
        long found = db.em.createQuery("select count(d) from BillingDocumentEntity d where trim(d.documentType)=:type and " + predicate, Long.class)
                .setParameter("type",document.getDocumentType().strip())
                .setParameter("number",(numbered ? document.getDocumentNumber() : document.getExternalNumber()).strip()).getSingleResult();
        if (found > 0) return OptionalLong.empty();
        var row = new BillingDocumentEntity();
        EntityMapper.copy(document,row);
        row.customer = customerFor(document.getCustomer());
        db.em.persist(row);
        writeChildren(row, document);
        db.em.flush();
        return OptionalLong.of(row.id);
    }

    private CustomerEntity customerFor(Customer customer) {
        var existing = db.em.createQuery("from CustomerEntity where identificationNumber=:n",CustomerEntity.class)
                .setParameter("n",customer.getIdentificationNumber()).getResultList();
        CustomerEntity parent;
        if (existing.isEmpty()) {
            parent = new CustomerEntity();
            EntityMapper.copy(customer,parent);
            db.em.persist(parent);
        } else {
            parent = existing.getFirst();
        }
        return parent;
    }

    public OptionalLong lockRequestForDocument(long id) {
        db.lockWriters();
        db.get(BillingDocumentEntity.class,id);
        return db.em.createQuery("select r.id from IntegrationRequestEntity r where r.document.id=:id", Long.class)
                .setParameter("id",id).getResultList().stream().mapToLong(Long::longValue).findFirst();
    }

    public void replace(long id, BillingDocument document) {
        var row = db.get(BillingDocumentEntity.class,id);
        EntityMapper.copy(document,row);
        row.customer = customerFor(document.getCustomer());
        writeChildren(row, document);
        db.em.flush();
    }

    public void delete(long id) {
        db.em.flush(); // La solicitud ya liberó su FK en esta transacción.
        var row = db.get(BillingDocumentEntity.class,id);
        for (var type : List.of(ItemEntity.class,PaymentEntity.class,AdjustmentEntity.class,HealthBillingDataEntity.class))
            children(type,id).forEach(db.em::remove);
        db.em.remove(row);
        db.em.flush();
    }

    private void writeChildren(BillingDocumentEntity row, BillingDocument document) {
        sync(children(ItemEntity.class,row.id), document.getItems(), ItemEntity::new,
                (child, value) -> { EntityMapper.copy(value, child); child.document = row; });
        sync(children(PaymentEntity.class,row.id), document.getPayments(), PaymentEntity::new,
                (child, value) -> { EntityMapper.copy(value, child); child.document = row; });
        sync(children(AdjustmentEntity.class,row.id), document.getAdjustments(), AdjustmentEntity::new,
                (child, value) -> { EntityMapper.copy(value, child); child.document = row; });
        sync(children(HealthBillingDataEntity.class,row.id), document.getHealthBillingData(), HealthBillingDataEntity::new,
                (child, value) -> { EntityMapper.copy(value, child); child.document = row; });
    }

    private <E,V> void sync(List<E> existing, List<V> values, java.util.function.Supplier<E> factory,
                            java.util.function.BiConsumer<E,V> copy) {
        for (int i=0; i<values.size(); i++) {
            boolean added = i >= existing.size();
            E child = added ? factory.get() : existing.get(i);
            copy.accept(child,values.get(i));
            if (added) db.em.persist(child);
        }
        for (int i=values.size(); i<existing.size(); i++) db.em.remove(existing.get(i));
    }

    private <T> List<T> children(Class<T> type,long id) {
        return db.em.createQuery("from " + type.getSimpleName() + " where document.id=:id order by id",type)
                .setParameter("id",id).getResultList();
    }
    public BillingDocument get(long id) {
        return EntityMapper.toDomain(db.get(BillingDocumentEntity.class,id),
                children(ItemEntity.class,id).stream().map(EntityMapper::toDomain).toList(),
                children(PaymentEntity.class,id).stream().map(EntityMapper::toDomain).toList(),
                children(AdjustmentEntity.class,id).stream().map(EntityMapper::toDomain).toList(),
                children(HealthBillingDataEntity.class,id).stream().map(EntityMapper::toDomain).toList());
    }

    public List<BillingDocument> list() {
        return db.all(BillingDocumentEntity.class).stream().map(row -> get(row.id)).toList();
    }
    public List<Published> forDate(LocalDate date,int page,int size) {
        db.lockWriters();
        var query = db.em.createQuery("select r.id, d.id from BillingDocumentEntity d left join IntegrationRequestEntity r on r.document=d where d.issueDate=:date order by d.id", Object[].class)
                .setParameter("date",date);
        if (page > 0) {
            long offset = (long)(page-1)*size;
            if (offset > Integer.MAX_VALUE) return List.of();
            query.setFirstResult((int)offset).setMaxResults(size);
        }
        return query.getResultList().stream().map(ids -> new Published((Long) ids[0], get((Long) ids[1]))).toList();
    }
}
