package usta.infrastructure.config;

import java.util.List;
import java.util.Map;

/** Rutas admitidas por los dos contratos externos. Los valores se parametrizan en PostgreSQL. */
public final class MappingFields {
    private MappingFields() {}
    private static final Map<String,List<String>> SOURCE = Map.of(
            "clientes",List.of("paciente.tipo_identificacion", "paciente.numero_identificacion", "paciente.digito_verificacion", "paciente.nombre", "paciente.direccion", "paciente.telefono", "paciente.correo", "paciente.codigo_municipio"),
            "documentos",List.of("factura.numero_interno", "factura.tipo_documento", "factura.fecha_emision", "factura.fecha_vencimiento", "factura.sede_atencion", "factura.observaciones", "factura.documento_afectado", "factura.periodo_facturado_desde", "factura.periodo_facturado_hasta"),
            "items",List.of("atenciones.codigo_servicio", "atenciones.nombre_servicio", "atenciones.cantidad", "atenciones.valor_unitario", "atenciones.porcentaje_iva"),
            "forma_pago",List.of("pagos.metodo", "pagos.referencia", "pagos.fecha", "pagos.valor", "responsable_pago.tipo", "responsable_pago.nombre", "responsable_pago.nit"),
            "ajustes",List.of("descuentos.tipo", "descuentos.descripcion", "descuentos.valor", "recargos.tipo", "recargos.descripcion", "recargos.valor"),
            "datos_salud",List.of("informacion_facturacion.modalidad_pago", "informacion_facturacion.cobertura_plan_beneficios", "informacion_facturacion.numero_contrato", "informacion_facturacion.numero_poliza", "informacion_facturacion.copago", "informacion_facturacion.cuota_moderadora", "informacion_facturacion.pagos_compartidos", "informacion_facturacion.anticipos")
    );
    private static final Map<String,List<String>> TARGET = Map.of(
            "clientes",List.of("nit", "dv", "claseid", "nom1", "nom2", "ape1", "ape2", "nombrec", "dir", "tel", "email", "ciudad"),
            "documentos",List.of("tipo", "numero", "noext", "afecta", "fecha", "fechaven", "plazo", "fpago", "nit", "vendedor", "bodega", "detalle", "obs", "ccosto", "succosto", "periodo", "desde", "hasta", "agente"),
            "items",List.of("referencia", "servicio", "cant", "precio", "vrunit", "vrtotal", "piva", "vriva", "vrico"),
            "forma_pago",List.of("fpago", "fechaven", "cuota", "vrfpago", "tercero", "ctafpago"),
            "ajustes",List.of("it", "cpto", "detalle", "valor"),
            "datos_salud",List.of("modalidad_pago", "cobertura_plan_beneficios", "numero_contrato", "numero_poliza", "copago", "cuota_moderadora", "pagos_compartidos", "anticipos")
    );

    public static boolean supports(String category,String source,String target) {
        return SOURCE.getOrDefault(category,List.of()).contains(source)
                && TARGET.getOrDefault(category,List.of()).contains(target);
    }
    public static Map<String,Object> options() {
        return Map.of("categoria",SOURCE.keySet().stream().sorted().toList(),"campo_origen",SOURCE,"campo_destino",TARGET);
    }
}
