package usta.application.command;

/** El adaptador REST debe traducir este fallo de recepción a HTTP 400. */
public final class InvalidPayload extends IllegalArgumentException {
    public InvalidPayload(String message) { super(message); }
}
