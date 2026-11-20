package usta.infrastructure.adapter.in.rest;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<org.hibernate.exception.ConstraintViolationException> {
    public Response toResponse(org.hibernate.exception.ConstraintViolationException failure) {
        return Response.status(409).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", "Conflicto con una restricción de persistencia.")).build();
    }
}
