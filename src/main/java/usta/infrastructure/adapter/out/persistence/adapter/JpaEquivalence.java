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
public class JpaEquivalence implements EquivalenceRepository {
    @Inject JpaAccess db;

    public List<Equivalence> list() {
        return db.all(EquivalenceEntity.class).stream().map(EntityMapper::toDomain).toList();
    }

    public Equivalence get(long id) { return EntityMapper.toDomain(db.get(EquivalenceEntity.class, id)); }

    public Equivalence saveUnique(Equivalence value) {
        db.lockWriters();
        var row = value.getId() == null ? new EquivalenceEntity() : db.get(EquivalenceEntity.class, value.getId());
        EntityMapper.copy(value, row);
        if (row.id == null) db.em.persist(row);
        db.em.flush();
        return EntityMapper.toDomain(row);
    }
    public void delete(long id) { db.lockWriters(); db.em.remove(db.get(EquivalenceEntity.class,id)); }
    public boolean supports(String category, String source, String target) {
        return usta.infrastructure.config.MappingFields.supports(category,source,target);
    }
}
