package usta.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;

/** Fila única que se bloquea (SELECT ... FOR UPDATE) para serializar las escrituras de facturación. */
@Entity
@Table(name = "writer_lock")
public class WriterLockEntity {
    @Id
    public Integer id;
}
