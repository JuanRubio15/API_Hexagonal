package usta.infrastructure.adapter.out.persistence.adapter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import usta.application.port.out.UserRepository;
import usta.domain.model.User;
import usta.infrastructure.adapter.out.persistence.entity.UserEntity;
import usta.infrastructure.adapter.out.persistence.mapper.EntityMapper;

@ApplicationScoped
@Transactional
public class JpaUser implements UserRepository {
    @Inject JpaAccess db;

    public Optional<User> findByUsername(String username) {
        return db.em.createQuery("from UserEntity u where u.username = :name", UserEntity.class)
                .setParameter("name", username).getResultStream().findFirst().map(EntityMapper::toDomain);
    }

    public User get(long id) { return EntityMapper.toDomain(db.get(UserEntity.class, id)); }

    public List<User> list() {
        return db.all(UserEntity.class).stream().map(EntityMapper::toDomain).toList();
    }

    public User save(User value) {
        db.lockWriters();
        var row = value.getId() == null ? new UserEntity() : db.get(UserEntity.class, value.getId());
        EntityMapper.copy(value, row);
        if (row.id == null) db.em.persist(row);
        db.em.flush();
        return EntityMapper.toDomain(row);
    }

    public void delete(long id) {
        db.lockWriters();
        db.em.remove(db.get(UserEntity.class, id));
    }

    public long count() {
        return db.em.createQuery("select count(u) from UserEntity u", Long.class).getSingleResult();
    }

    public long countActiveAdministrators() {
        return db.em.createQuery("select count(u) from UserEntity u where u.role = 'ADMINISTRATOR' and u.active = true", Long.class)
                .getSingleResult();
    }

    public void lockChanges() { db.lockWriters(); }
}
