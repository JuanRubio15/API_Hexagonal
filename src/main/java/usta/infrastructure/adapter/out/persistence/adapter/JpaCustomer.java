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
public class JpaCustomer implements CustomerRepository {
    @Inject JpaAccess db;

    public List<Customer> list() {
        return db.all(CustomerEntity.class).stream().map(EntityMapper::toDomain).toList();
    }

    public Customer get(long id) { return EntityMapper.toDomain(db.get(CustomerEntity.class, id)); }

    public Customer save(Customer value) {
        db.lockWriters();
        var row = value.getId() == null ? new CustomerEntity() : db.get(CustomerEntity.class, value.getId());
        EntityMapper.copy(value, row);
        if (row.id == null) db.em.persist(row);
        db.em.flush();
        return EntityMapper.toDomain(row);
    }
    public boolean deleteIfUnused(long id) {
        db.lockWriters();
        var row = db.get(CustomerEntity.class,id);
        long count = db.em.createQuery("select count(d) from BillingDocumentEntity d where d.customer.id=:id",Long.class)
                .setParameter("id",id).getSingleResult();
        if (count != 0) return false;
        db.em.remove(row);
        return true;
    }
}
