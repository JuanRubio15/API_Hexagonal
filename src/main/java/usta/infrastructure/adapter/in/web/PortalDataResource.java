package usta.infrastructure.adapter.in.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;
import usta.domain.model.Permission;
import usta.infrastructure.security.RequiresPermission;
import usta.application.port.in.QueryIntegration;
import usta.domain.model.BillingDocument;
import usta.infrastructure.adapter.in.json.BillingJson;

/** Proyecciones del portal. Consultarlas no registra entregas a SysCafé. */
@Path("/portal-data")
@Produces(MediaType.APPLICATION_JSON)
@RequiresPermission(Permission.READ)
public class PortalDataResource {
    @Inject QueryIntegration queries;
    @Inject BillingJson json;

    @GET @Path("documents")
    public List<ObjectNode> documents() {
        return queries.documents().stream().map(this::summary).toList();
    }

    private ObjectNode summary(BillingDocument document) {
        var row = com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.objectNode();
        row.put("document_id", document.getId());
        row.put("numero", document.getDocumentNumber());
        row.put("noext", document.getExternalNumber());
        row.put("tipo", document.getDocumentType());
        row.put("fecha", document.getIssueDate().toString());
        row.put("sede", document.getWarehouseCode());
        row.put("total", document.netAmount());
        var customer = document.getCustomer();
        row.put("nit", customer.getIdentificationNumber());
        row.put("cliente", customer.getDisplayName() == null ? customer.getFirstName() : customer.getDisplayName());
        var methods = row.putArray("methods");
        document.getPayments().stream().map(p -> p.getPaymentMethodCode()).filter(java.util.Objects::nonNull)
                .distinct().forEach(methods::add);
        var payers = row.putArray("payers");
        document.getPayments().stream().map(p -> p.getPayerIdentification()).filter(java.util.Objects::nonNull)
                .distinct().forEach(payers::add);
        return row;
    }

    @GET @Path("documents/{id}")
    public ObjectNode detail(@PathParam("id") long id) {
        var document = queries.document(id);
        var result = summary(document);
        result.set("document", json.external(document));
        return result;
    }

    @GET @Path("documents/{id}/editor")
    public Map<String, String> editor(@PathParam("id") long id) {
        return Map.of("json", json.external(queries.document(id)).toPrettyString());
    }
}
