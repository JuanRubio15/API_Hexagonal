package usta.infrastructure.adapter.in.rest;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import usta.application.port.in.ManageUsers;
import usta.domain.model.Permission;
import usta.domain.model.Role;
import usta.domain.model.User;
import usta.infrastructure.security.RequiresPermission;

@Path("/usuarios")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name="Usuarios")
@RequiresPermission(Permission.MANAGE)
public class UserResource {
    @Inject ManageUsers users;

    // Nunca se devuelve el hash de la contraseña
    private static Map<String,Object> view(User user) {
        var row = new LinkedHashMap<String,Object>();
        row.put("id", user.getId());
        row.put("usuario", user.getUsername());
        row.put("rol", user.getRole().name());
        row.put("activo", user.isActive());
        return row;
    }

    @GET @Operation(summary="Listar usuarios")
    public List<Map<String,Object>> list() { return users.list().stream().map(UserResource::view).toList(); }

    @GET @Path("{id}") @Operation(summary="Ver un usuario")
    public Map<String,Object> get(@PathParam("id") long id) { return view(users.get(id)); }

    @POST @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary="Crear un usuario (rol: ADMINISTRADOR, OPERADOR o VISUALIZADOR)")
    public Response create(JsonNode body) {
        if (body == null || !body.isObject()) throw new IllegalArgumentException("Se esperaba un objeto JSON.");
        var created = users.create(body.path("usuario").asText(), body.path("password").asText(null), Role.parse(body.path("rol").asText(null)));
        return Response.status(201).entity(view(created)).build();
    }

    @PUT @Path("{id}") @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary="Cambiar rol, estado o contraseña (los campos que no se envían no cambian)")
    public Map<String,Object> update(@PathParam("id") long id, JsonNode body) {
        if (body == null || !body.isObject()) throw new IllegalArgumentException("Se esperaba un objeto JSON.");
        Role role = body.hasNonNull("rol") ? Role.parse(body.get("rol").asText()) : null;
        Boolean active = body.hasNonNull("activo") ? body.get("activo").asBoolean() : null;
        String password = body.hasNonNull("password") ? body.get("password").asText() : null;
        return view(users.update(id, role, active, password));
    }

    @DELETE @Path("{id}") @Operation(summary="Eliminar un usuario")
    public void delete(@PathParam("id") long id) { users.delete(id); }
}
