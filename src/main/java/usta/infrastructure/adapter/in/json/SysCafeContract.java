package usta.infrastructure.adapter.in.json;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;
import usta.application.port.out.DocumentContract;
import usta.domain.model.*;

@ApplicationScoped
public class SysCafeContract implements DocumentContract {
    @Inject BillingJson json;
    private static final Map<String,Integer> LIM_DOC = Map.ofEntries(
            Map.entry("tipo",3),
            Map.entry("numero",10),
            Map.entry("noext",15),
            Map.entry("afecta",13),
            Map.entry("fecha",10),
            Map.entry("fechaven",10),
            Map.entry("fpago",3),
            Map.entry("nit",16),
            Map.entry("vendedor",16),
            Map.entry("bodega",3),
            Map.entry("detalle",80),
            Map.entry("obs",255),
            Map.entry("ccosto",3),
            Map.entry("succosto",3),
            Map.entry("periodo",6),
            Map.entry("desde",10),
            Map.entry("hasta",10),
            Map.entry("agente",16));
    private static final Map<String,Integer> LIM_CLIENTE = Map.ofEntries(
            Map.entry("nit",20),
            Map.entry("dv",1),
            Map.entry("claseid",1),
            Map.entry("nom1",80),
            Map.entry("nom2",20),
            Map.entry("ape1",20),
            Map.entry("ape2",20),
            Map.entry("nombrec",80),
            Map.entry("dir",60),
            Map.entry("tel",30),
            Map.entry("email",60),
            Map.entry("ciudad",5));
    private static final Map<String,Integer> LIM_ITEM = Map.ofEntries(
            Map.entry("referencia",15),
            Map.entry("servicio",8));
    private static final Map<String,Integer> LIM_FPAGO = Map.ofEntries(
            Map.entry("tercero",16),
            Map.entry("fpago",3),
            Map.entry("fechaven",10),
            Map.entry("cuota",3),
            Map.entry("ctafpago",15));
    private static final Map<String,Integer> LIM_AJUSTE = Map.ofEntries(
            Map.entry("it",1),
            Map.entry("cpto",3),
            Map.entry("detalle",60));
    private static final Map<String,Integer> LIM_SALUD = Map.ofEntries(
            Map.entry("modalidad_pago",2),
            Map.entry("cobertura_plan_beneficios",2),
            Map.entry("numero_contrato",60),
            Map.entry("numero_poliza",60));

    public List<String> errors(BillingDocument d,List<Equivalence> mappings,boolean fromIps) {
        var errors = new ArrayList<String>();
        JsonNode n = json.external(d);
        lengths(n,LIM_DOC,"documento",errors);
        errors.addAll(componentErrors("clientes",n.path("cliente"),mappings));
        for (var section : Map.of("items","items","fpagos","fpagos","itvr","ajustes","itfes","datos-salud").entrySet()) {
            int i = 0;
            for (var row : n.path(section.getKey())) {
                String path = section.getKey()+"["+(++i)+"]";
                for (var message : componentErrors(section.getValue(),row,mappings)) errors.add(path+": "+message);
            }
        }
        if (d.getBillingPeriod() != null && !d.getBillingPeriod().matches("[0-9]{4}(0[1-9]|1[0-2])")) errors.add("Período inválido.");
        if (d.getPaymentTermDays() != null && d.getPaymentTermDays() > 99999) errors.add("Plazo fuera de rango.");
        if (fromIps && d.getDocumentType().length()!=3) errors.add("Tipo IPS inválido.");
        catalog(mappings,"documentos","tipo",d.getDocumentType(),errors);
        catalog(mappings,"documentos","bodega",d.getWarehouseCode(),errors);
        if (fromIps && d.getItems().stream().anyMatch(i -> i.getServiceCode().length()!=3))
            errors.add("El servicio IPS requiere tres caracteres.");
        return errors;
    }

    public List<String> componentErrors(String resource,JsonNode row,List<Equivalence> mappings) {
        var errors = new ArrayList<String>();
        switch(resource) {
            case "clientes" -> {
                lengths(row,LIM_CLIENTE,resource,errors);
                if (!row.path("nit").asText().matches("[0-9]+")) errors.add("La identificación requiere dígitos.");
                if (row.hasNonNull("email") && !row.get("email").asText().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+"))
                    errors.add("Correo inválido.");
                String type = row.path("claseid").asText();
                if (!Set.of("N","C","E","T","P","A").contains(type)) errors.add("Clase de identificación inválida.");
                catalog(mappings,"clientes","claseid",type,errors);
            }
            case "items" -> {
                lengths(row,LIM_ITEM,resource,errors);
                catalog(mappings,"items","servicio",row.path("servicio").asText(null),errors);
            }
            case "fpagos" -> {
                lengths(row,LIM_FPAGO,resource,errors);
                String account = row.path("ctafpago").asText();
                if (!account.matches("[0-9]+")) errors.add("La cuenta contable requiere dígitos.");
                pair(mappings,"forma_pago","fpago",row.path("fpago").asText(),"ctafpago",account,errors);
            }
            case "ajustes" -> {
                lengths(row,LIM_AJUSTE,resource,errors);
                pair(mappings,"ajustes","it",row.path("it").asText(),"cpto",row.path("cpto").asText(),errors);
            }
            case "datos-salud" -> lengths(row,LIM_SALUD,resource,errors);
            case "equivalencias" -> lengths(row,Map.of("categoria",50,"campo_origen",100,"campo_destino",100,
                    "valor_origen",255,"valor_destino",255),resource,errors);
            default -> throw new NoSuchElementException("Recurso desconocido.");
        }
        return errors;
    }

    private void lengths(JsonNode n, Map<String,Integer> limits, String path,List<String> errors) {
        limits.forEach((key,max) -> {
            String value = n.path(key).asText("");
            if (value.codePointCount(0,value.length()) > max) errors.add(path+"."+key+" supera "+max+" caracteres.");
        });
    }
    private void catalog(List<Equivalence> rows,String category,String key,String value,List<String> errors) {
        if (value == null) return;
        var options = rows.stream().filter(e -> category.equals(e.getCategory()) && key.equals(e.getTargetField())
                && e.getTargetValue()!=null).map(Equivalence::getTargetValue).toList();
        if (!options.isEmpty() && !options.contains(value)) errors.add("Código desconocido en "+key+": "+value);
    }
    private void pair(List<Equivalence> rows,String category,String a,String av,String b,String bv,List<String> errors) {
        var candidates = rows.stream().filter(e -> category.equals(e.getCategory()) && a.equals(e.getTargetField())
                && e.getSourceValue()!=null).toList();
        if (candidates.isEmpty()) return;
        for (var e : candidates) {
            if (av.equals(e.getTargetValue()) && rows.stream().anyMatch(other -> category.equals(other.getCategory())
                    && e.getSourceField().equals(other.getSourceField()) && e.getSourceValue().equals(other.getSourceValue())
                    && b.equals(other.getTargetField()) && bv.equals(other.getTargetValue()))) return;
        }
        errors.add("Combinación inválida: "+a+" / "+b);
    }
}
