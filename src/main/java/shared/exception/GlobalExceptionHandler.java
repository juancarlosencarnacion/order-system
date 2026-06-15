package shared.exception;

import java.time.LocalDateTime;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import shared.model.ErrorResponse;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Context
    ContainerRequestContext request;

    @Override
    public Response toResponse(Exception ex) {

        int status = 500;

        if (ex instanceof NotFoundException) {
            status = 404;
        } else if (ex instanceof BadRequestException) {
            status = 400;
        } else if (ex instanceof InvalidOrderStatusException) { 
            // Capturamos tu nueva excepción de negocio y le asignamos un 400 (o 409 Conflict)
            status = 409; 
        }

        ErrorResponse error = new ErrorResponse(
                status,
                ex.getMessage(),
                request.getUriInfo().getPath(),
                LocalDateTime.now());

        return Response.status(status)
                .entity(error)
                .build();
    }

}
