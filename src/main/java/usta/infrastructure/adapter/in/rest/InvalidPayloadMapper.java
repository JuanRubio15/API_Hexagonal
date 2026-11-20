package usta.infrastructure.adapter.in.rest;

import jakarta.ws.rs.core.*;
import jakarta.ws.rs.ext.*;
import java.util.Map;
import usta.application.command.InvalidPayload;

@Provider
public class InvalidPayloadMapper implements ExceptionMapper<InvalidPayload> {
    @Override
    public Response toResponse(InvalidPayload failure) {
        return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                .entity(Map.of("error", failure.getMessage())).build();
    }
}
