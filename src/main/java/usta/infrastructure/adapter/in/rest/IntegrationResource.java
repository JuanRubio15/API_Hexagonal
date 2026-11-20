package usta.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.*;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.*;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import usta.infrastructure.adapter.in.json.BillingJson;
import usta.infrastructure.adapter.in.json.JsonPayloadStructure;
import usta.infrastructure.security.Credentials;
import usta.application.port.in.*;
import usta.domain.model.Permission;
import usta.domain.model.User;
import usta.infrastructure.security.RequiresPermission;
import usta.domain.model.IntegrationRequest;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name="Integración")
public class IntegrationResource {
    @Inject ProcessDocuments processing;
    @Inject QueryIntegration queries;
    @Inject ManageStagedDocuments stagedDocuments;
    private final JsonPayloadStructure structure = new JsonPayloadStructure();
    @Inject BillingJson json;
    @Inject Credentials credentials;
    @Context SecurityContext security;
    private String actor() { return security.getUserPrincipal().getName(); }

    @POST @Path("login") @Consumes(MediaType.APPLICATION_JSON)
    public Response login(JsonNode body) {
        Optional<User> user = body==null ? Optional.empty()
                : credentials.login(body.path("usuario").asText(),body.path("password").asText());
        if (user.isEmpty()) return Response.status(401).build();
        return Response.ok(Map.of("token",credentials.issue(user.get()),"tipo","Bearer",
                "usuario",user.get().getUsername(),"rol",user.get().getRole().name())).build();
    }
    @POST @RequiresPermission(Permission.CONVERT) @Path("staging") @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary="Recibir y procesar el JSON original")
    public Response receive(String raw) {
        var result = processing.receive(json.receive(raw,actor()));
        int code = switch(result.status()) { case CONVERTED -> 201; case PENDING -> 202; case REJECTED -> 409;
            case DELETED -> 0;
        };
        return Response.status(code).entity(result).build();
    }
    @GET @RequiresPermission(Permission.READ) @Produces(MediaType.TEXT_HTML) @Operation(summary="Abrir el panel de facturación")
    public Response home() { return Response.seeOther(URI.create("/panel")).build(); }

    @GET @RequiresPermission(Permission.READ) @Path("staging") public Object documents() { return queries.documents(); }
    @GET @RequiresPermission(Permission.READ) @Path("solicitudes") public Object requests() { return queries.requests(); }
    @GET @RequiresPermission(Permission.READ) @Path("solicitudes/{id}") public Object request(@PathParam("id") long id) { return queries.request(id); }
    @GET @RequiresPermission(Permission.READ) @Path("solicitudes/{id}/historial") public Object history(@PathParam("id") long id) { return queries.history(id); }
    @POST @RequiresPermission(Permission.CONVERT) @Path("solicitudes/{id}/reprocesar") public Object retry(@PathParam("id") long id) { return processing.retry(id,actor()); }
    @GET @RequiresPermission(Permission.READ) @Path("alertas") public Object alerts() { return queries.alerts(); }
    @GET @RequiresPermission(Permission.READ) @Path("GetFacturas") @Operation(summary="Documentos disponibles para la importación de SysCafé")
    public Object invoices(@QueryParam("fecha") String date,@QueryParam("consec") @DefaultValue("0") int page,
                           @QueryParam("op") String reserved) {
        if(date==null) throw new IllegalArgumentException("La fecha es obligatoria.");
        return queries.forSysCafe(LocalDate.parse(date),page,actor()).stream().map(json::external).toList();
    }
    @PUT @RequiresPermission(Permission.MANAGE) @Path("staging/{id}") @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary="Actualizar un documento convertido")
    public Response updateDocument(@PathParam("id") long id, String raw) {
        stagedDocuments.replace(id, json.preparedDocument(structure.readPrepared(raw)), actor());
        return Response.noContent().build();
    }

    @DELETE @RequiresPermission(Permission.MANAGE) @Path("staging/{id}")
    @Operation(summary="Eliminar un documento convertido")
    public Response deleteDocument(@PathParam("id") long id) {
        stagedDocuments.delete(id, actor());
        return Response.noContent().build();
    }
}
