package usta.application.port.out;

/** Comprueba la forma del contenido antes de aceptar la recepción por cualquier adaptador. */
public interface PayloadStructure {
    void validate(String payload);
}
