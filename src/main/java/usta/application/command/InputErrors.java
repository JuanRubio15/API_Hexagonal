package usta.application.command;

import java.util.List;

/** Reúne problemas corregibles de una entrada o de sus equivalencias. */
public final class InputErrors extends IllegalArgumentException {
    private final List<String> messages;
    public InputErrors(List<String> messages) {
        super(String.join("; ", messages));
        this.messages = List.copyOf(messages);
        if (messages.isEmpty()) throw new IllegalArgumentException("Se requiere al menos un error.");
    }
    public List<String> messages() { return messages; }
}
