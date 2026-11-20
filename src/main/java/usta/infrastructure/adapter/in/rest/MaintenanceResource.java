package usta.infrastructure.adapter.in.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import com.fasterxml.jackson.databind.JsonNode;
import usta.infrastructure.adapter.in.Maintenance;
import usta.domain.model.Permission;
import usta.infrastructure.security.RequiresPermission;

@Path("/{resource:clientes|items|fpagos|ajustes|datos-salud|equivalencias}")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name="Mantenimiento")
@RequiresPermission(Permission.MANAGE)
public class MaintenanceResource {
    @Inject Maintenance manager;
    @PathParam("resource") String resource;
    @GET @RequiresPermission(Permission.READ) public Object list(@QueryParam("categoria") String category,@QueryParam("campo_origen") String source,
                            @QueryParam("campo_destino") String target) {
        var rows = manager.list(resource);
        if (!resource.equals("equivalencias")) return rows;
        return rows.stream().filter(row -> category==null || category.equals(row.path("categoria").asText()))
                .filter(row -> source==null || source.equals(row.path("campo_origen").asText()))
                .filter(row -> target==null || target.equals(row.path("campo_destino").asText())).toList();
    }
    @GET @Path("enums") public Object options() {
        if (!resource.equals("equivalencias")) throw new NotFoundException();
        return usta.infrastructure.config.MappingFields.options();
    }
    @GET @RequiresPermission(Permission.READ) @Path("{id}") public Object get(@PathParam("id") long id) { return manager.get(resource,id); }
    @POST @Consumes(MediaType.APPLICATION_JSON) public Response create(JsonNode data) {
        return Response.status(201).entity(manager.save(resource,null,data)).build();
    }
    @PUT @Path("{id}") @Consumes(MediaType.APPLICATION_JSON)
    public Object update(@PathParam("id") long id,JsonNode data) { return manager.save(resource,id,data); }
    @DELETE @Path("{id}") public void delete(@PathParam("id") long id) { manager.delete(resource,id); }
}
