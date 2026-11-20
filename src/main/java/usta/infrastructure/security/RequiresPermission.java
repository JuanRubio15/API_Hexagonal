package usta.infrastructure.security;

import java.lang.annotation.*;
import usta.domain.model.Permission;

/** Permiso mínimo para llamar a un recurso. En un método manda sobre el de la clase. Sin anotación: solo administrador. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface RequiresPermission {
    Permission value();
}
