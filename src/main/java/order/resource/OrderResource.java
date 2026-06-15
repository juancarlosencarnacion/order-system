package order.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import order.dto.OrderRequest;
import order.service.OrderService;

@Path("api/v1/orders")
@RequiredArgsConstructor
@Produces(MediaType.APPLICATION_JSON) // Centraliza las respuestas JSON
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private final OrderService orderService;

    @POST
    public Response createOrder(OrderRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(orderService.createOrder(request))
                .build();
    }

    @POST
    @Path("/{id}/complete")
    public Response completeOrder(@PathParam("id") Long id) {
        return Response.ok(orderService.completeOrder(id)).build();
    }

    @GET
    public Response getOrders() {
        return Response.ok(orderService.getOrders()).build();
    }

    @GET
    @Path("/{id}")
    public Response getOrderById(@PathParam("id") Long id) {
        return Response.ok(orderService.getOrderById(id)).build();
    }

}
