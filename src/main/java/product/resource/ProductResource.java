package product.resource;

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
import product.dto.ProductRequest;
import product.service.ProductService;

@Path("/api/v1/products")
@RequiredArgsConstructor
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    private final ProductService productService;

    @POST
    public Response createProduct(ProductRequest request) {
        return Response.status(Response.Status.CREATED)
                .entity(productService.createProduct(request))
                .build();
    }

    @GET
    public Response getProducts() {
        return Response.ok(productService.getProducts()).build();
    }

    @GET
    @Path("/{id}")
    public Response getProductById(@PathParam("id") Long id) {
        return Response.ok(productService.getProductById(id)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") Long id){
        productService.deleteProduct(id);
        return Response.noContent().build();
    }

}
