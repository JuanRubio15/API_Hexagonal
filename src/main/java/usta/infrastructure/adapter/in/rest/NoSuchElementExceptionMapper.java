package usta.infrastructure.adapter.in.rest;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;

@Provider
public class NoSuchElementExceptionMapper implements ExceptionMapper<java.util.NoSuchElementException> {
    public Response toResponse(java.util.NoSuchElementException failure) {
        return Response.status(404).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", String.valueOf(failure.getMessage()))).build();
    }
}
