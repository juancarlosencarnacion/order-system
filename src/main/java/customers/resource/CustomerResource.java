package customers.resource;

import customers.dto.CustomerRequest;
import customers.service.CustomerService;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;

@Path("/api/v1/customers")
@RequiredArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    private final CustomerService customerService;

    @POST
    public Response createCustomer(CustomerRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(customerService.createCustomer(request))
                .build();
    }

    @GET
    public Response getCustomers() {
        return Response.ok(customerService.getCustomers())
                .build();
    }

    @GET
    @Path("/{id}")
    public Response getCustomerById(@PathParam("id") Long id) {
        return Response.ok(customerService.getCustomerById(id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteCustomer(@PathParam("id") Long id) {
        customerService.deleteCustomer(id);
        return Response.noContent().build();
    }
}
