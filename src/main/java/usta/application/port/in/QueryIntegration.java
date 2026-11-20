package usta.application.port.in;

import java.time.LocalDate;
import java.util.List;
import usta.domain.model.*;

public interface QueryIntegration {
    List<IntegrationRequest> requests();
    IntegrationRequest request(long id);
    List<ProcessingEvent> history(long requestId);
    List<IntegrationRequest> alerts();
    List<BillingDocument> documents();
    BillingDocument document(long id);
    List<BillingDocument> forSysCafe(LocalDate date, int page, String actor);
    void deleteDocument(long id);

}
