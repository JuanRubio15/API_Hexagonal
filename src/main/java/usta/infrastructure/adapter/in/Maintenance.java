package usta.infrastructure.adapter.in;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.*;
import usta.application.port.in.*;
import usta.domain.model.*;
import usta.infrastructure.adapter.in.json.BillingJson;

/** Traduce las operaciones de mantenimiento compartidas por consola, navegador y REST. */
@ApplicationScoped
public class Maintenance {
    @Inject BillingJson json;
    @Inject usta.infrastructure.adapter.in.json.SysCafeContract contract;
    @Inject QueryIntegration queries;
    @Inject ManageCustomers customers;
    @Inject ManageEquivalences equivalences;
    @Inject ManageDocumentDetails<Item> items;
    @Inject ManageDocumentDetails<Payment> payments;
    @Inject ManageDocumentDetails<Adjustment> adjustments;
    @Inject ManageDocumentDetails<HealthBillingData> health;
    public static final List<String> RESOURCES = List.of("clientes","equivalencias","items","fpagos","ajustes","datos-salud");

    private ObjectNode identified(ObjectNode data,long id) {
        data.put("id",id);
        return data;
    }
    public List<ObjectNode> list(String resource) {
        return switch(resource) {
            case "clientes" -> customers.list().stream().map(v -> identified(json.external(v),v.getId())).toList();
            case "equivalencias" -> equivalences.list().stream().map(v -> identified(json.external(v),v.getId())).toList();
            case "items" -> items.list().stream().map(v -> identified(json.external(v),v.getId())).toList();
            case "fpagos" -> payments.list().stream().map(v -> identified(json.external(v),v.getId())).toList();
            case "ajustes" -> adjustments.list().stream().map(v -> identified(json.external(v),v.getId())).toList();
            case "datos-salud" -> health.list().stream().map(v -> identified(json.external(v),v.getId())).toList();
            default -> throw new NoSuchElementException("Recurso desconocido.");
        };
    }
    public ObjectNode get(String resource,long id) {
        return switch(resource) {
            case "clientes" -> identified(json.external(customers.get(id)),id);
            case "equivalencias" -> identified(json.external(equivalences.get(id)),id);
            case "items" -> identified(json.external(items.get(id)),id);
            case "fpagos" -> identified(json.external(payments.get(id)),id);
            case "ajustes" -> identified(json.external(adjustments.get(id)),id);
            case "datos-salud" -> identified(json.external(health.get(id)),id);
            default -> throw new NoSuchElementException("Recurso desconocido.");
        };
    }
    public ObjectNode save(String resource,Long id,JsonNode input) {
        if (input == null || !input.isObject()) throw new IllegalArgumentException("Se esperaba un objeto JSON.");
        var issues = contract.componentErrors(resource,input,equivalences.list());
        if (!issues.isEmpty()) throw new IllegalArgumentException(String.join("; ",issues));
        return switch(resource) {
            case "clientes" -> {
                var value = customers.save(json.customer(input,id));
                yield identified(json.external(value),value.getId());
            }
            case "equivalencias" -> {
                var value = equivalences.save(json.equivalence(input,id));
                yield identified(json.external(value),value.getId());
            }
            case "items" -> {
                long owner;
                if (input.path("document_id").canConvertToLong() && input.path("document_id").isIntegralNumber()) owner = input.get("document_id").longValue();
                else if (id != null && !input.has("document_id")) owner = queries.documents().stream()
                        .filter(d -> d.getItems().stream().anyMatch(x -> id.equals(x.getId())))
                        .findFirst().orElseThrow().getId();
                else throw new IllegalArgumentException("Indique document_id entero.");
                var value = items.save(owner,json.item(input,id));
                yield identified(json.external(value),value.getId());
            }
            case "fpagos" -> {
                long owner;
                if (input.path("document_id").canConvertToLong() && input.path("document_id").isIntegralNumber()) owner = input.get("document_id").longValue();
                else if (id != null && !input.has("document_id")) owner = queries.documents().stream()
                        .filter(d -> d.getPayments().stream().anyMatch(x -> id.equals(x.getId())))
                        .findFirst().orElseThrow().getId();
                else throw new IllegalArgumentException("Indique document_id entero.");
                var value = payments.save(owner,json.payment(input,id));
                yield identified(json.external(value),value.getId());
            }
            case "ajustes" -> {
                long owner;
                if (input.path("document_id").canConvertToLong() && input.path("document_id").isIntegralNumber()) owner = input.get("document_id").longValue();
                else if (id != null && !input.has("document_id")) owner = queries.documents().stream()
                        .filter(d -> d.getAdjustments().stream().anyMatch(x -> id.equals(x.getId())))
                        .findFirst().orElseThrow().getId();
                else throw new IllegalArgumentException("Indique document_id entero.");
                var value = adjustments.save(owner,json.adjustment(input,id));
                yield identified(json.external(value),value.getId());
            }
            case "datos-salud" -> {
                long owner;
                if (input.path("document_id").canConvertToLong() && input.path("document_id").isIntegralNumber()) owner = input.get("document_id").longValue();
                else if (id != null && !input.has("document_id")) owner = queries.documents().stream()
                        .filter(d -> d.getHealthBillingData().stream().anyMatch(x -> id.equals(x.getId())))
                        .findFirst().orElseThrow().getId();
                else throw new IllegalArgumentException("Indique document_id entero.");
                var value = health.save(owner,json.healthBillingData(input,id));
                yield identified(json.external(value),value.getId());
            }
            default -> throw new NoSuchElementException("Recurso desconocido.");
        };
    }
    public void delete(String resource,long id) {
        switch(resource) {
            case "clientes" -> customers.delete(id);
            case "equivalencias" -> equivalences.delete(id);
            case "items" -> items.delete(id);
            case "fpagos" -> payments.delete(id);
            case "ajustes" -> adjustments.delete(id);
            case "datos-salud" -> health.delete(id);
            default -> throw new NoSuchElementException("Recurso desconocido.");
        }
    }
}
