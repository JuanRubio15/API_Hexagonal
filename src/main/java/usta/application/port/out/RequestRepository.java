package usta.application.port.out;

import java.util.*;
import usta.domain.model.*;

public interface RequestRepository {
    IntegrationRequest save(IntegrationRequest request);
    /** Bloqueo exclusivo hasta terminar la transacción. */
    IntegrationRequest getForUpdate(long id);
    IntegrationRequest get(long id);
    List<IntegrationRequest> list();
    List<ProcessingEvent> history(long requestId);
    void append(ProcessingEvent event);
    /** Solicitudes con fallo de conversión vigente o pendientes anteriores al límite. */
    List<IntegrationRequest> needingAttention(java.time.Instant cutoff);
}
