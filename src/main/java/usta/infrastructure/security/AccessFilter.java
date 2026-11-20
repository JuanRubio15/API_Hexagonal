package usta.infrastructure.security;

import io.smallrye.common.annotation.Blocking;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.*;
import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.Provider;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import usta.domain.model.Permission;

@Provider
@Priority(Priorities.AUTHENTICATION)
@Blocking // consulta la base de datos para validar al usuario
public class AccessFilter implements ContainerRequestFilter {
    @Inject Credentials credentials;
    @Context ResourceInfo resource;

    public void filter(ContainerRequestContext request) {
        String path = request.getUriInfo().getPath().replaceFirst("^/+", "");
        if (path.equals("login") && request.getMethod().equals("POST")) return;
        String auth = request.getHeaderString("Authorization");
        AuthenticatedUser who = null;
        if (auth != null && auth.startsWith("Bearer ")) {
            String token = auth.substring(7);
            if (path.equals("GetFacturas") && request.getMethod().equals("GET") && credentials.consumer(token)) who = AuthenticatedUser.consumer("SysCafé");
            else who = credentials.fromToken(token).map(AuthenticatedUser::of).orElse(null);
        } else if (auth != null && auth.startsWith("Basic ")) {
            try {
                String[] pair = new String(Base64.getDecoder().decode(auth.substring(6)),StandardCharsets.UTF_8).split(":",2);
                if (pair.length==2) who = credentials.login(pair[0],pair[1]).map(AuthenticatedUser::of).orElse(null);
            } catch (IllegalArgumentException ignored) { }
        }
        if (who == null) {
            request.abortWith(Response.status(401).header("WWW-Authenticate","Basic realm=\"Facturación IPS\"").build());
            return;
        }
        boolean safeMethod = java.util.Set.of("GET","HEAD","OPTIONS").contains(request.getMethod());
        if (!safeMethod && (auth.startsWith("Basic ") || path.startsWith("panel"))) {
            URI base = request.getUriInfo().getBaseUri();
            String origin = base.getScheme()+"://"+base.getRawAuthority();
            if (!origin.equals(request.getHeaderString("Origin"))) {
                request.abortWith(Response.status(403).build());
                return;
            }
        }
        // El consumidor externo ya quedó limitado arriba a GET /GetFacturas; el resto se rige por el rol.
        if (!who.isConsumer() && !who.can(requiredPermission())) {
            request.abortWith(Response.status(403).type(MediaType.TEXT_PLAIN_TYPE)
                    .entity("Su rol no tiene permiso para esta operación.").build());
            return;
        }
        AuthenticatedUser principal = who;
        boolean secure = request.getSecurityContext().isSecure();
        request.setSecurityContext(new SecurityContext() {
            public Principal getUserPrincipal() { return principal; }
            public boolean isUserInRole(String role) { return principal.isInRole(role); }
            public boolean isSecure() { return secure; }
            public String getAuthenticationScheme() { return auth.startsWith("Bearer ") ? "Bearer" : BASIC_AUTH; }
        });
    }

    // Lo que diga el método manda sobre la clase; sin anotación se exige MANAGE (denegar por defecto).
    private Permission requiredPermission() {
        var method = resource.getResourceMethod();
        var annotation = method == null ? null : method.getAnnotation(RequiresPermission.class);
        if (annotation == null) annotation = resource.getResourceClass().getAnnotation(RequiresPermission.class);
        return annotation == null ? Permission.MANAGE : annotation.value();
    }
}
