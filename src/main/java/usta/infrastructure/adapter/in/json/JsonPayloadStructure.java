package usta.infrastructure.adapter.in.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import usta.application.command.InvalidPayload;
import usta.application.port.out.PayloadStructure;

/** Verifica ambos contratos de entrada sin consultar catálogos ni aplicar reglas de facturación. */
public final class JsonPayloadStructure implements PayloadStructure {
    private final ObjectReader json = new ObjectMapper().reader()
            .with(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);

    public void validate(String payload) {
        JsonNode root = readObject(payload, json);
        if (root.has("factura") || root.has("paciente") || root.has("atenciones") || root.has("sistema_origen")) {
            ips(root);
        } else {
            prepared(root);
        }
    }

    public JsonNode readPrepared(String payload) {
        JsonNode root = readObject(payload, json.with(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS));
        prepared(root);
        return root;
    }

    private JsonNode readObject(String payload, ObjectReader reader) {
        if (payload == null || payload.isBlank()) throw new InvalidPayload("El cuerpo está vacío.");
        JsonNode root;
        try {
            root = reader.readTree(payload);
        } catch (JsonProcessingException e) {
            throw new InvalidPayload("JSON mal formado.");
        }
        object(root, "$");
        return root;
    }

    private void ips(JsonNode root) {
        fields(root, "$", "sistema_origen!", "", "");
        fields(child(root, "factura"), "factura", "numero_interno! tipo_documento! sede_atencion observaciones documento_afectado", "",
                "fecha_emision! fecha_vencimiento periodo_facturado_desde periodo_facturado_hasta");
        fields(child(root, "paciente"), "paciente", "tipo_identificacion! numero_identificacion! nombre! digito_verificacion direccion telefono correo codigo_municipio", "", "");
        array(root, "atenciones", true, "codigo_servicio! nombre_servicio", "cantidad! valor_unitario! porcentaje_iva", "");
        optionalObject(root, "responsable_pago", "tipo nombre nit", "", "");
        optionalObject(root, "pago", "metodo! referencia", "valor!", "fecha");
        array(root, "pagos", false, "metodo! referencia", "valor!", "fecha");
        for (String key : new String[]{"descuentos", "recargos"}) {
            array(root, key, false, "tipo! descripcion", "valor!", "");
        }
        optionalObject(root, "informacion_facturacion", HEALTH_TEXT, HEALTH_NUMBERS, "");
    }

    private static final String HEALTH_TEXT = "modalidad_pago cobertura_plan_beneficios numero_contrato numero_poliza";
    private static final String HEALTH_NUMBERS = "copago cuota_moderadora pagos_compartidos anticipos";

    private void prepared(JsonNode root) {
        fields(root, "$", "tipo! nit! numero noext afecta fpago vendedor bodega detalle obs ccosto succosto periodo agente",
                "plazo", "fecha! fechaven desde hasta");
        fields(child(root, "cliente"), "cliente", "nit! claseid! nom1! dv nom2 ape1 ape2 nombrec dir tel email ciudad", "", "");
        array(root, "items", true, "referencia servicio", "cant! precio vrunit! vrtotal! piva! vriva vrico", "");
        array(root, "fpagos", false, "fpago! ctafpago! cuota tercero", "vrfpago!", "fechaven");
        array(root, "itvr", false, "it cpto! detalle", "valor!", "");
        array(root, "itfes", false, HEALTH_TEXT, HEALTH_NUMBERS, "");
    }

    private static void fields(JsonNode node, String path, String texts, String numbers, String dates) {
        object(node, path);
        check(node, path, texts, Kind.TEXT);
        check(node, path, numbers, Kind.NUMBER);
        check(node, path, dates, Kind.DATE);
    }

    private enum Kind { TEXT, NUMBER, DATE }

    // El sufijo ! identifica un atributo obligatorio en el contrato de entrada.
    private static void check(JsonNode node, String path, String specification, Kind kind) {
        if (specification.isEmpty()) return;
        for (String token : specification.split(" ")) {
            boolean required = token.endsWith("!");
            String key = required ? token.substring(0, token.length() - 1) : token;
            JsonNode value = node.path(key);
            if (value.isMissingNode() || value.isNull()) {
                if (required) throw new InvalidPayload("Falta " + path + "." + key);
                continue;
            }
            boolean valid = kind == Kind.NUMBER ? value.isNumber() : value.isTextual();
            if (!valid || (required && value.isTextual() && value.textValue().isBlank())) {
                throw new InvalidPayload("Tipo o valor obligatorio inválido en " + path + "." + key);
            }
            if (kind == Kind.DATE && !value.textValue().isBlank()) {
                try {
                    LocalDate.parse(value.textValue());
                } catch (DateTimeParseException e) {
                    throw new InvalidPayload("Fecha inválida en " + path + "." + key);
                }
            }
        }
    }

    private static void object(JsonNode value, String path) {
        if (value == null || !value.isObject()) throw new InvalidPayload("Se esperaba un objeto en " + path);
    }

    private static JsonNode child(JsonNode root, String key) {
        JsonNode value = root.get(key);
        object(value, key);
        return value;
    }

    private static void optionalObject(JsonNode root, String key, String text, String numbers, String dates) {
        if (root.hasNonNull(key)) fields(root.get(key), key, text, numbers, dates);
    }

    private static void array(JsonNode root, String key, boolean required, String text, String numbers, String dates) {
        if (!required && !root.hasNonNull(key)) return;
        JsonNode rows = root.path(key);
        if (!rows.isArray()) throw new InvalidPayload("Se esperaba una lista en " + key);
        for (int i = 0; i < rows.size(); i++) fields(rows.get(i), key + "[" + i + "]", text, numbers, dates);
    }
}
