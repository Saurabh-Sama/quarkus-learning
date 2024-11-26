package saurabh.learning;

import static jakarta.ws.rs.core.Response.Status.CREATED;
import static jakarta.ws.rs.core.Response.Status.NOT_FOUND;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import saurabh.learning.model.Product;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import java.util.List;

@Path("/products")
@ApplicationScoped
@Produces("application/json")
@Consumes("application/json")
public class ProductResource {

    // Get all products
    @GET
    public Uni<List<Product>> getAllProducts() {
        return Product.listAll(); // Retrieve all products
    }

    // Get a specific product by ID
    @GET
    @Path("/{id}")
    public Uni<Response> getProductById(@PathParam("id") Long id) {
        return Product.findById(id)
                .onItem().ifNotNull().transform(product -> Response.ok(product).build())
                .onItem().ifNull().continueWith(Response.status(NOT_FOUND).build());
    }

    // Create a new product
    @POST
    public Uni<Response> createProduct(Product product) {
        if (product == null || product.id != null) {
            throw new WebApplicationException("Id was invalidly set on request.", 422);
        }

        return Panache.withTransaction(product::persist)
                    .replaceWith(Response.ok(product).status(CREATED)::build);
    }

    // Update an existing product
    @PUT
    @Path("/{id}")
    public Uni<Response> updateProduct(@PathParam("id") Long id, Product updatedProduct) {
        if (updatedProduct == null || updatedProduct.name == null) {
            throw new WebApplicationException("Product name was not set on request.", 422);
        }

        return Panache.withTransaction(() -> Product.findById(id)
                .onItem().ifNotNull().invoke(existingProduct -> {
                    Product product = (Product) existingProduct;  // Cast to Product
                    product.name = updatedProduct.name;
                    product.description = updatedProduct.description;
                    product.price = updatedProduct.price;
                    product.quantity = updatedProduct.quantity;
                }))
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.status(NOT_FOUND).build());
    }

    // Delete product by ID
    @DELETE
    @Path("/{id}")
    public Uni<Response> deleteProduct(@PathParam("id") Long id) {
        return Panache.withTransaction(() -> Product.deleteById(id))
                .map(deleted -> deleted
                        ? Response.noContent().build() // Successfully deleted
                        : Response.status(NOT_FOUND).build()); // Product not found
    }

    // Check stock availability for a product by ID
    @GET
    @Path("/{id}/check-stock")
    public Uni<Response> checkStock(@PathParam("id") Long id, @QueryParam("count") int count) {
        return Product.findById(id)
                .onItem().ifNotNull().transform(product -> {
                	Product product1 = (Product) product;
                    boolean isAvailable = product1.quantity >= count;
                    return Response.ok(isAvailable).build();
                })
                .onItem().ifNull().continueWith(Response.status(NOT_FOUND).build());
    }

    // Get all products sorted by price in ascending order
    @GET
    @Path("/sorted-by-price")
    public Uni<List<Product>> getProductsSortedByPrice() {
        return Product.find("ORDER BY price ASC").list(); // Retrieves products sorted by price
    }
}
