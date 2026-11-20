package usta.infrastructure.adapter.in.json;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.*;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.function.Function;
import usta.domain.model.*;
import usta.application.command.*;
import usta.application.port.out.PayloadReader;

@ApplicationScoped
public class BillingJson implements PayloadReader {
    @Inject ObjectMapper mapper;

    public JsonNode parse(String json) {
        try { return mapper.readTree(json); }
        catch (com.fasterxml.jackson.core.JsonProcessingException e) { throw new InvalidPayload("JSON mal formado."); }
    }
    private static String text(JsonNode node,String field) {
        var value = node.get(field);
        if (value == null || value.isNull()) return null;
        if (!value.isTextual()) throw new InvalidPayload("Se esperaba texto en " + field);
        return value.textValue().isBlank() ? null : value.textValue().strip();
    }
    private static LocalDate date(JsonNode node,String field) {
        String value = text(node,field);
        return value == null ? null : LocalDate.parse(value);
    }
    private static BigDecimal number(JsonNode node,String field) {
        var value = node.get(field);
        if (value == null || value.isNull()) return null;
        if (!value.isNumber()) throw new InvalidPayload("Se esperaba un número en " + field);
        return value.decimalValue();
    }
    private static Integer integer(JsonNode n,String key) {
        var value = number(n,key);
        if (value == null) return null;
        try { return value.intValueExact(); }
        catch (ArithmeticException e) { throw new InvalidPayload("Se requiere entero en " + key); }
    }
    private static <T> List<T> rows(JsonNode node, String field, Function<JsonNode,T> read) {
        var list = new ArrayList<T>();
        for (var entry : node.path(field)) list.add(read.apply(entry));
        return list;
    }

    public ReceiveDocument receive(String json,String actor) {
        var root = parse(json);
        String system = root != null && root.path("sistema_origen").isTextual() ? text(root,"sistema_origen") : null;
        String number = root == null ? null : root.path("factura").path("numero_interno").asText(null);
        return new ReceiveDocument(system,number,json,actor);
    }

    public Input read(String json) {
        var n = parse(json);
        if (!n.has("factura")) {
            return new Prepared(preparedDocument(n));
        }
        var h = n.path("factura");
        var p = n.path("paciente");
        var header = new InvoiceInput.Header(text(h,"numero_interno"),text(h,"tipo_documento"),date(h,"fecha_emision"),
                date(h,"fecha_vencimiento"),date(h,"periodo_facturado_desde"),date(h,"periodo_facturado_hasta"),
                text(h,"sede_atencion"),text(h,"observaciones"),text(h,"documento_afectado"));
        var patient = new InvoiceInput.Patient(text(p,"tipo_identificacion"),text(p,"numero_identificacion"),
                text(p,"digito_verificacion"),text(p,"nombre"),text(p,"direccion"),text(p,"telefono"),
                text(p,"correo"),text(p,"codigo_municipio"));
        var payments = rows(n,"pagos",this::ipsPayment);
        if (!n.hasNonNull("pagos") && n.hasNonNull("pago")) payments.add(ipsPayment(n.get("pago")));
        var health = n.path("informacion_facturacion");
        return new Ips(new InvoiceInput(header,patient,text(n.path("responsable_pago"),"nit"),
                rows(n,"atenciones",x -> new InvoiceInput.Line(text(x,"codigo_servicio"),number(x,"cantidad"),
                        number(x,"valor_unitario"),number(x,"porcentaje_iva"))),payments,
                rows(n,"descuentos",this::ipsAdjustment),rows(n,"recargos",this::ipsAdjustment),
                health.isObject() && !health.isEmpty() ? healthBillingData(health,null) : null));
    }
    private InvoiceInput.PaymentInput ipsPayment(JsonNode n) {
        return new InvoiceInput.PaymentInput(text(n,"metodo"),date(n,"fecha"),number(n,"valor"));
    }
    private InvoiceInput.AdjustmentInput ipsAdjustment(JsonNode n) {
        return new InvoiceInput.AdjustmentInput(text(n,"tipo"),text(n,"descripcion"),number(n,"valor"));
    }

    public Customer customer(JsonNode n, Long id) {
        return Customer.builder().id(id)
                .identificationNumber(text(n,"nit"))
                .identificationType(text(n,"claseid"))
                .verificationDigit(text(n,"dv"))
                .firstName(text(n,"nom1"))
                .middleName(text(n,"nom2"))
                .firstSurname(text(n,"ape1"))
                .secondSurname(text(n,"ape2"))
                .displayName(text(n,"nombrec"))
                .address(text(n,"dir"))
                .phone(text(n,"tel"))
                .email(text(n,"email"))
                .cityCode(text(n,"ciudad")).build();
    }

    public ObjectNode external(Customer value) {
        var result = mapper.createObjectNode();
        if (value.getIdentificationNumber() != null) result.put("nit",value.getIdentificationNumber());
        if (value.getIdentificationType() != null) result.put("claseid",value.getIdentificationType());
        if (value.getVerificationDigit() != null) result.put("dv",value.getVerificationDigit());
        if (value.getFirstName() != null) result.put("nom1",value.getFirstName());
        if (value.getMiddleName() != null) result.put("nom2",value.getMiddleName());
        if (value.getFirstSurname() != null) result.put("ape1",value.getFirstSurname());
        if (value.getSecondSurname() != null) result.put("ape2",value.getSecondSurname());
        if (value.getDisplayName() != null) result.put("nombrec",value.getDisplayName());
        if (value.getAddress() != null) result.put("dir",value.getAddress());
        if (value.getPhone() != null) result.put("tel",value.getPhone());
        if (value.getEmail() != null) result.put("email",value.getEmail());
        if (value.getCityCode() != null) result.put("ciudad",value.getCityCode());
        return result;
    }

    public Item item(JsonNode n, Long id) {
        return Item.builder().id(id)
                .itemReference(text(n,"referencia"))
                .serviceCode(text(n,"servicio"))
                .quantity(number(n,"cant"))
                .price(number(n,"precio"))
                .unitAmount(number(n,"vrunit"))
                .lineAmount(number(n,"vrtotal"))
                .vatRate(number(n,"piva"))
                .vatAmount(number(n,"vriva"))
                .consumptionTaxAmount(number(n,"vrico")).build();
    }

    public ObjectNode external(Item value) {
        var result = mapper.createObjectNode();
        if (value.getItemReference() != null) result.put("referencia",value.getItemReference());
        if (value.getServiceCode() != null) result.put("servicio",value.getServiceCode());
        if (value.getQuantity() != null) result.put("cant",value.getQuantity());
        if (value.getPrice() != null) result.put("precio",value.getPrice());
        if (value.getUnitAmount() != null) result.put("vrunit",value.getUnitAmount());
        if (value.getLineAmount() != null) result.put("vrtotal",value.getLineAmount());
        if (value.getVatRate() != null) result.put("piva",value.getVatRate());
        if (value.getVatAmount() != null) result.put("vriva",value.getVatAmount());
        if (value.getConsumptionTaxAmount() != null) result.put("vrico",value.getConsumptionTaxAmount());
        return result;
    }

    public Payment payment(JsonNode n, Long id) {
        return Payment.builder().id(id)
                .paymentMethodCode(text(n,"fpago"))
                .ledgerAccount(text(n,"ctafpago"))
                .amount(number(n,"vrfpago"))
                .dueDate(date(n,"fechaven"))
                .installmentNumber(text(n,"cuota"))
                .payerIdentification(text(n,"tercero")).build();
    }

    public ObjectNode external(Payment value) {
        var result = mapper.createObjectNode();
        if (value.getPaymentMethodCode() != null) result.put("fpago",value.getPaymentMethodCode());
        if (value.getLedgerAccount() != null) result.put("ctafpago",value.getLedgerAccount());
        if (value.getAmount() != null) result.put("vrfpago",value.getAmount());
        if (value.getDueDate() != null) result.put("fechaven",value.getDueDate().toString());
        if (value.getInstallmentNumber() != null) result.put("cuota",value.getInstallmentNumber());
        if (value.getPayerIdentification() != null) result.put("tercero",value.getPayerIdentification());
        return result;
    }

    public Adjustment adjustment(JsonNode n, Long id) {
        return Adjustment.builder().id(id)
                .adjustmentType(text(n,"it"))
                .conceptCode(text(n,"cpto"))
                .description(text(n,"detalle"))
                .amount(number(n,"valor")).build();
    }

    public ObjectNode external(Adjustment value) {
        var result = mapper.createObjectNode();
        if (value.getAdjustmentType() != null) result.put("it",value.getAdjustmentType());
        if (value.getConceptCode() != null) result.put("cpto",value.getConceptCode());
        if (value.getDescription() != null) result.put("detalle",value.getDescription());
        if (value.getAmount() != null) result.put("valor",value.getAmount());
        return result;
    }

    public HealthBillingData healthBillingData(JsonNode n, Long id) {
        return HealthBillingData.builder().id(id)
                .paymentModality(text(n,"modalidad_pago"))
                .benefitPlanCoverage(text(n,"cobertura_plan_beneficios"))
                .contractNumber(text(n,"numero_contrato"))
                .policyNumber(text(n,"numero_poliza"))
                .copayment(number(n,"copago"))
                .moderatingFee(number(n,"cuota_moderadora"))
                .sharedPayments(number(n,"pagos_compartidos"))
                .advances(number(n,"anticipos")).build();
    }

    public ObjectNode external(HealthBillingData value) {
        var result = mapper.createObjectNode();
        if (value.getPaymentModality() != null) result.put("modalidad_pago",value.getPaymentModality());
        if (value.getBenefitPlanCoverage() != null) result.put("cobertura_plan_beneficios",value.getBenefitPlanCoverage());
        if (value.getContractNumber() != null) result.put("numero_contrato",value.getContractNumber());
        if (value.getPolicyNumber() != null) result.put("numero_poliza",value.getPolicyNumber());
        if (value.getCopayment() != null) result.put("copago",value.getCopayment());
        if (value.getModeratingFee() != null) result.put("cuota_moderadora",value.getModeratingFee());
        if (value.getSharedPayments() != null) result.put("pagos_compartidos",value.getSharedPayments());
        if (value.getAdvances() != null) result.put("anticipos",value.getAdvances());
        return result;
    }

    public Equivalence equivalence(JsonNode n, Long id) {
        return Equivalence.builder().id(id)
                .category(text(n,"categoria"))
                .sourceField(text(n,"campo_origen"))
                .targetField(text(n,"campo_destino"))
                .sourceValue(text(n,"valor_origen"))
                .targetValue(text(n,"valor_destino")).build();
    }

    public ObjectNode external(Equivalence value) {
        var result = mapper.createObjectNode();
        if (value.getCategory() != null) result.put("categoria",value.getCategory());
        if (value.getSourceField() != null) result.put("campo_origen",value.getSourceField());
        if (value.getTargetField() != null) result.put("campo_destino",value.getTargetField());
        if (value.getSourceValue() != null) result.put("valor_origen",value.getSourceValue());
        if (value.getTargetValue() != null) result.put("valor_destino",value.getTargetValue());
        return result;
    }

    public BillingDocument preparedDocument(JsonNode n) {
        var document = billingDocument(n,null);
        if (!document.getCustomer().getIdentificationNumber().equals(text(n,"nit")))
            throw new IllegalArgumentException("El NIT de cabecera difiere del cliente.");
        return document;
    }

    public BillingDocument billingDocument(JsonNode n, Long id) {
        return BillingDocument.builder().id(id)
                .documentType(text(n,"tipo"))
                .documentNumber(text(n,"numero"))
                .externalNumber(text(n,"noext"))
                .affectedDocument(text(n,"afecta"))
                .issueDate(date(n,"fecha"))
                .dueDate(date(n,"fechaven"))
                .paymentTermDays(integer(n,"plazo"))
                .paymentMethodCode(text(n,"fpago"))
                .sellerIdentification(text(n,"vendedor"))
                .warehouseCode(text(n,"bodega"))
                .description(text(n,"detalle"))
                .notes(text(n,"obs"))
                .costCenter(text(n,"ccosto"))
                .costSubcenter(text(n,"succosto"))
                .billingPeriod(text(n,"periodo"))
                .periodStart(date(n,"desde"))
                .periodEnd(date(n,"hasta"))
                .agentIdentification(text(n,"agente"))
                .customer(customer(n.path("cliente"),null))
                .items(rows(n,"items",x -> item(x,null)))
                .payments(rows(n,"fpagos",x -> payment(x,null)))
                .adjustments(rows(n,"itvr",x -> adjustment(x,null)))
                .healthBillingData(rows(n,"itfes",x -> healthBillingData(x,null))).build();
    }

    public ObjectNode external(BillingDocument value) {
        var result = mapper.createObjectNode();
        if (value.getDocumentType() != null) result.put("tipo",value.getDocumentType());
        if (value.getDocumentNumber() != null) result.put("numero",value.getDocumentNumber());
        if (value.getExternalNumber() != null) result.put("noext",value.getExternalNumber());
        if (value.getAffectedDocument() != null) result.put("afecta",value.getAffectedDocument());
        if (value.getIssueDate() != null) result.put("fecha",value.getIssueDate().toString());
        if (value.getDueDate() != null) result.put("fechaven",value.getDueDate().toString());
        if (value.getPaymentTermDays() != null) result.put("plazo",value.getPaymentTermDays());
        if (value.getPaymentMethodCode() != null) result.put("fpago",value.getPaymentMethodCode());
        if (value.getSellerIdentification() != null) result.put("vendedor",value.getSellerIdentification());
        if (value.getWarehouseCode() != null) result.put("bodega",value.getWarehouseCode());
        if (value.getDescription() != null) result.put("detalle",value.getDescription());
        if (value.getNotes() != null) result.put("obs",value.getNotes());
        if (value.getCostCenter() != null) result.put("ccosto",value.getCostCenter());
        if (value.getCostSubcenter() != null) result.put("succosto",value.getCostSubcenter());
        if (value.getBillingPeriod() != null) result.put("periodo",value.getBillingPeriod());
        if (value.getPeriodStart() != null) result.put("desde",value.getPeriodStart().toString());
        if (value.getPeriodEnd() != null) result.put("hasta",value.getPeriodEnd().toString());
        if (value.getAgentIdentification() != null) result.put("agente",value.getAgentIdentification());
        result.put("nit",value.getCustomer().getIdentificationNumber());
        result.set("cliente",external(value.getCustomer()));
        var items = result.putArray("items");
        value.getItems().forEach(detail -> items.add(external(detail)));
        var payments = result.putArray("fpagos");
        value.getPayments().forEach(detail -> payments.add(external(detail)));
        var adjustments = result.putArray("itvr");
        value.getAdjustments().forEach(detail -> adjustments.add(external(detail)));
        var healthBillingData = result.putArray("itfes");
        value.getHealthBillingData().forEach(detail -> healthBillingData.add(external(detail)));
        return result;
    }
}
