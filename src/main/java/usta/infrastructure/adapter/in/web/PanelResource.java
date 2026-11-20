package usta.infrastructure.adapter.in.web;

import io.quarkus.qute.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import usta.application.port.in.*;
import usta.infrastructure.adapter.in.Maintenance;
import usta.infrastructure.adapter.in.json.BillingJson;
import usta.infrastructure.security.AuthenticatedUser;
import usta.domain.model.Permission;
import usta.infrastructure.security.RequiresPermission;

@Path("/panel")
@Produces(MediaType.TEXT_HTML)
@Tag(name="Panel de facturación")
public class PanelResource {
    @org.eclipse.microprofile.config.inject.ConfigProperty(name="invoice.issuer.name", defaultValue="Nombre de la empresa")
    String issuerName;
    @org.eclipse.microprofile.config.inject.ConfigProperty(name="invoice.issuer.nit", defaultValue="NIT por configurar")
    String issuerNit;
    @org.eclipse.microprofile.config.inject.ConfigProperty(name="invoice.issuer.address", defaultValue="Dirección por configurar")
    String issuerAddress;

    @Inject Template panel;
    @Inject Template intake;
    @Inject Template detail;
    @Inject Template maintenance;
    @Inject QueryIntegration queries;
    @Inject ProcessDocuments processing;
    @Inject Maintenance manager;
    @Inject BillingJson json;
    @Context SecurityContext security;

    @GET @RequiresPermission(Permission.READ) @Operation(summary="Listar solicitudes, facturas y alertas")
    public TemplateInstance index() {
        return view(panel.data("requests",queries.requests(),"documents",queries.documents(),"alerts",queries.alerts()));
    }
    @GET @RequiresPermission(Permission.CONVERT) @Path("recibir") @Operation(summary="Abrir el formulario de recepción")
    public TemplateInstance form() { return view(intake.data("original","","error","")); }
    @POST @RequiresPermission(Permission.CONVERT) @Path("recibir") @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response submit(@FormParam("payload") String raw) {
        try {
            var result = processing.receive(json.receive(raw,security.getUserPrincipal().getName()));
            return redirect("solicitudes/"+result.requestId());
        } catch (IllegalArgumentException error) {
            return Response.status(400).entity(view(intake.data("original",raw,"error",error.getMessage())).render()).build();
        }
    }
    @GET @RequiresPermission(Permission.READ) @Path("solicitudes/{id}") @Operation(summary="Ver el original, el documento preparado y el historial")
    public TemplateInstance detail(@PathParam("id") long id) {
        var request = queries.request(id);
        var doc = request.getDocumentId()==null ? null : queries.document(request.getDocumentId());
        return view(detail.data("request",request,"events",queries.history(id),"prepared",doc==null ? "" : json.external(doc).toPrettyString()));
    }
    @POST @RequiresPermission(Permission.CONVERT) @Path("solicitudes/{id}/reprocesar")
    public Response retry(@PathParam("id") long id) {
        processing.retry(id,security.getUserPrincipal().getName());
        return redirect("solicitudes/"+id);
    }
    @GET @RequiresPermission(Permission.MANAGE) @Path("gestion") @Operation(summary="Gestionar clientes, equivalencias y detalles")
    public TemplateInstance manage(@QueryParam("resource") @DefaultValue("equivalencias") String resource,
                                   @QueryParam("id") Long id) {
        String input = id == null ? example(resource) : manager.get(resource,id).toPrettyString();
        return management(resource,id,input,"","");
    }
    private String example(String resource) {
        String fields = switch(resource) {
            case "equivalencias" -> "{\"categoria\": \"items\", \"campo_origen\": \"atenciones.codigo_servicio\", \"campo_destino\": \"servicio\", \"valor_origen\": \"\", \"valor_destino\": \"\"}";
            case "clientes" -> "{\"nit\": \"\", \"claseid\": \"C\", \"nom1\": \"\"}";
            case "items" -> "{\"document_id\": 1, \"servicio\": \"001\", \"cant\": 1, \"vrunit\": 0, \"vrtotal\": 0, \"piva\": 0}";
            case "fpagos" -> "{\"document_id\": 1, \"fpago\": \"001\", \"ctafpago\": \"110505\", \"vrfpago\": 1}";
            case "ajustes" -> "{\"document_id\": 1, \"it\": \"D\", \"cpto\": \"DCV\", \"valor\": 0}";
            case "datos-salud" -> "{\"document_id\": 1, \"copago\": 0, \"cuota_moderadora\": 0}";
            default -> throw new java.util.NoSuchElementException("Recurso desconocido.");
        };
        return json.parse(fields).toPrettyString();
    }
    private TemplateInstance management(String resource,Long id,String input,String message,String error) {
        return view(maintenance.data("resources",Maintenance.RESOURCES,"resource",resource,"id",id,"input",input)
                .data("rows",manager.list(resource)).data("message",message).data("error",error));
    }
    @POST @RequiresPermission(Permission.MANAGE) @Path("gestion") @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance change(@FormParam("resource") String resource,@FormParam("id") Long id,
            @FormParam("payload") String raw,@FormParam("action") String action) {
        try {
            if ("delete".equals(action)) manager.delete(resource,id);
            else manager.save(resource,id,json.parse(raw));
            return management(resource,null,example(resource),"Operación guardada.","");
        } catch (RuntimeException e) {
            return management(resource,id,raw,"", e instanceof jakarta.persistence.PersistenceException
                    ? "Conflicto con un registro existente." : String.valueOf(e.getMessage()));
        }
    }
    @GET @RequiresPermission(Permission.READ) @Path("reportes/procesos.pdf") @Produces("application/pdf")
    @Operation(summary="Descargar el historial de procesamiento en PDF")
    public Response report() { return pdf(PdfReports.processing(queries),"procesos.pdf"); }
    @GET @RequiresPermission(Permission.READ) @Path("facturas/{id}/documento.pdf") @Produces("application/pdf")
    @Operation(summary="Descargar la representación del documento")
    public Response documentPdf(@PathParam("id") long id) { return pdf(PdfReports.invoice(queries.document(id), new PdfReports.Issuer(issuerName, issuerNit, issuerAddress)),"factura-"+id+".pdf"); }

    // Las plantillas muestran u ocultan los enlaces según lo que el rol puede hacer
    private TemplateInstance view(TemplateInstance page) {
        var who = (AuthenticatedUser) security.getUserPrincipal();
        return page.data("canConvert",who.can(Permission.CONVERT)).data("canManage",who.can(Permission.MANAGE));
    }
    private Response pdf(byte[] data,String filename) {
        return Response.ok(data,"application/pdf").header("Content-Disposition","attachment; filename="+filename).build();
    }
    private Response redirect(String path) { return Response.seeOther(java.net.URI.create("/panel/"+path)).build(); }
}
