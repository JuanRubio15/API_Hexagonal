package usta.infrastructure.adapter.out.persistence.adapter;

import jakarta.persistence.*;
import jakarta.inject.Inject;

import java.util.*;

@jakarta.inject.Singleton
public class JpaAccess {
    @Inject EntityManager em;

    public <T> T get(Class<T> type, long id) {
        T row = em.find(type, id);
        if (row == null) throw new NoSuchElementException("Registro inexistente: " + id);
        return row;
    }

    // Serializa las escrituras de facturación en esta instalación académica.
    // También cubre documentos que todavía no tienen una fila que se pueda bloquear.
    // Compatible con MySQL/MariaDB: bloquea la fila única de writer_lock (la tabla la crea Hibernate).
    void lockWriters() {
        if (!lockRow()) {
            em.createNativeQuery("insert ignore into writer_lock (id) values (1)").executeUpdate();
            lockRow();
        }
    }

    private boolean lockRow() {
        return !em.createNativeQuery("select id from writer_lock where id = 1 for update").getResultList().isEmpty();
    }

    <T> List<T> all(Class<T> type) {
        return em.createQuery("from " + type.getSimpleName() + " order by id", type).getResultList();
    }
}
