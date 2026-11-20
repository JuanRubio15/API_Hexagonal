package usta.application.port.out;

import usta.application.command.InvoiceInput;
import usta.domain.model.BillingDocument;

/** Lee ambos formatos de staging. Los errores de contenido se comunican como InputErrors. */
public interface PayloadReader {
    Input read(String originalPayload);
    sealed interface Input permits Ips, Prepared {}
    record Ips(InvoiceInput invoice) implements Input {}
    record Prepared(BillingDocument document) implements Input {}
}
