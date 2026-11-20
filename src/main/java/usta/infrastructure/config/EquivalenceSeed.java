package usta.infrastructure.config;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import usta.infrastructure.adapter.out.persistence.entity.EquivalenceEntity;

/**
 * Carga las equivalencias iniciales (antes las sembraba Flyway con V2__equivalences.sql)
 * solo cuando la tabla está vacía. Después se administran desde /equivalencias.
 */
@ApplicationScoped
public class EquivalenceSeed {
    private static final String[][] ROWS = {
        {"documentos","factura.tipo_documento","tipo","factura_venta","FV1"},
        {"documentos","factura.tipo_documento","tipo","nota_credito","NC1"},
        {"documentos","factura.sede_atencion","bodega","Sede Villanueva","VIL"},
        {"documentos","factura.sede_atencion","bodega","Sede Centro","CEN"},
        {"documentos","factura.sede_atencion","bodega","Sede Norte","NOR"},
        {"clientes","paciente.tipo_identificacion","claseid","NIT","N"},
        {"clientes","paciente.tipo_identificacion","claseid","CC","C"},
        {"clientes","paciente.tipo_identificacion","claseid","CE","E"},
        {"clientes","paciente.tipo_identificacion","claseid","TI","T"},
        {"clientes","paciente.tipo_identificacion","claseid","RC","P"},
        {"clientes","paciente.tipo_identificacion","claseid","PA","A"},
        {"items","atenciones.codigo_servicio","servicio","CONS-001","001"},
        {"items","atenciones.codigo_servicio","servicio","LAB-015","015"},
        {"items","atenciones.codigo_servicio","servicio","IMG-020","020"},
        {"items","atenciones.codigo_servicio","servicio","TER-030","030"},
        {"items","atenciones.codigo_servicio","servicio","ODO-040","040"},
        {"forma_pago","pagos.metodo","fpago","efectivo","001"},
        {"forma_pago","pagos.metodo","fpago","tarjeta_credito","002"},
        {"forma_pago","pagos.metodo","fpago","transferencia_bancaria","003"},
        {"forma_pago","pagos.metodo","ctafpago","efectivo","110505"},
        {"forma_pago","pagos.metodo","ctafpago","tarjeta_credito","111010"},
        {"forma_pago","pagos.metodo","ctafpago","transferencia_bancaria","111005"},
        {"ajustes","descuentos.tipo","it","descuento_convenio","D"},
        {"ajustes","descuentos.tipo","it","descuento_pronto_pago","D"},
        {"ajustes","descuentos.tipo","cpto","descuento_convenio","DCV"},
        {"ajustes","descuentos.tipo","cpto","descuento_pronto_pago","DPP"},
        {"ajustes","recargos.tipo","it","recargo_mora","C"},
        {"ajustes","recargos.tipo","cpto","recargo_mora","RMO"},
    };

    @Inject EntityManager em;

    @Transactional
    void onStart(@Observes StartupEvent event) {
        long existing = em.createQuery("select count(e) from EquivalenceEntity e", Long.class).getSingleResult();
        if (existing > 0) return;
        for (String[] r : ROWS) {
            var row = new EquivalenceEntity();
            row.category = r[0]; row.sourceField = r[1]; row.targetField = r[2];
            row.sourceValue = r[3]; row.targetValue = r[4];
            em.persist(row);
        }
        Log.info("Se cargaron " + ROWS.length + " equivalencias iniciales.");
    }
}
