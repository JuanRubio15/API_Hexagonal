package usta.infrastructure.adapter.in.rest;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {
    public Response toResponse(IllegalArgumentException failure) {
        return Response.status(400).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", String.valueOf(failure.getMessage()))).build();
    }
}
