package usta.domain.model;

/** Acciones que el sistema controla. Cada rol tiene un subconjunto de ellas. */
public enum Permission {
    /** Ver el panel, las solicitudes, las facturas y los reportes PDF. */
    READ,
    /** Recibir documentos y convertirlos (o reprocesarlos). */
    CONVERT,
    /** Mantenimiento de clientes, equivalencias, detalles y usuarios. */
    MANAGE
}
