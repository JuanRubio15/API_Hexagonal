package usta.infrastructure.adapter.in.rest;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;

@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {
    public Response toResponse(IllegalStateException failure) {
        return Response.status(409).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", String.valueOf(failure.getMessage()))).build();
    }
}
