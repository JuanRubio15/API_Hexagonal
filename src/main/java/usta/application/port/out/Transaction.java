package usta.application.port.out;

import java.util.function.Supplier;

public interface Transaction {
    /** Si el trabajo falla, revierte todas sus escrituras. */
    <T> T run(Supplier<T> work);
}
