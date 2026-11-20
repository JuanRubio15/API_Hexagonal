package usta.application.port.in;

import usta.application.command.ReceiveDocument;
import usta.domain.model.IntegrationRequest.Status;
import java.util.List;

public interface ProcessDocuments {
    Result receive(ReceiveDocument input);
    Result retry(long requestId, String actor);
    record Result(long requestId, Status status, Long documentId, List<String> messages) {
        public Result { messages = List.copyOf(messages); }
    }
}
