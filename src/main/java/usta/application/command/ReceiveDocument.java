package usta.application.command;

/** Verifican que los datos recibidos de un sistema externo, sean apropiados,
 * para esto revisa los metadatos, que pueden faltar si la entrada está dañada. Se conserva el original. */
public record ReceiveDocument(
        String sourceSystem, String sourceNumber, String payload, String actor) {}
