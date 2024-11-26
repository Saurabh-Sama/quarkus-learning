package saurabh.learning;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class ProductResourceTest {

    // Test for creating a product
    @Test
    @Transactional
    public void testCreateProduct() {
        String productJson = "{ \"name\": \"Product 1\", \"description\": \"Description of Product 1\", \"price\": 99.99, \"quantity\": 50 }";

        given()
            .contentType(ContentType.JSON)
            .body(productJson)
            .when().post("/products")
            .then()
            .statusCode(201)
            .body("name", equalTo("Product 1"))
            .body("description", equalTo("Description of Product 1"))
            .body("price", equalTo(99.99f))
            .body("quantity", equalTo(50));
    }

    // Test for getting all products
    @Test
    public void testGetAllProducts() {
        given()
            .when().get("/products")
            .then()
            .statusCode(200)
            .body("$.size()", greaterThan(0));
    }

    // Test for getting a specific product by ID
    @Test
    public void testGetProductById() {
        // First create a product
        String productJson = "{ \"name\": \"Product 1\", \"description\": \"Description of Product 1\", \"price\": 99.99, \"quantity\": 50 }";
        Integer productId = given()
            .contentType(ContentType.JSON)
            .body(productJson)
            .when().post("/products")
            .then()
            .statusCode(201)
            .extract().path("id");

        // Get the product by ID
        given()
            .when().get("/products/" + productId)
            .then()
            .statusCode(200)
            .body("id", equalTo(productId))
            .body("name", equalTo("Product 1"));
    }

    // Test for updating a product
    @Test
    @Transactional
    public void testUpdateProduct() {
        // First create a product
        String productJson = "{ \"name\": \"Product 1\", \"description\": \"Description of Product 1\", \"price\": 99.99, \"quantity\": 50 }";
        Integer productId = given()
            .contentType(ContentType.JSON)
            .body(productJson)
            .when().post("/products")
            .then()
            .statusCode(201)
            .extract().path("id");

        // Update the product
        String updatedProductJson = "{ \"name\": \"Updated Product 1\", \"description\": \"Updated Description\", \"price\": 129.99, \"quantity\": 30 }";
        given()
            .contentType(ContentType.JSON)
            .body(updatedProductJson)
            .when().put("/products/" + productId)
            .then()
            .statusCode(200)
            .body("name", equalTo("Updated Product 1"))
            .body("description", equalTo("Updated Description"))
            .body("price", equalTo(129.99f))
            .body("quantity", equalTo(30));
    }

    // Test for deleting a product
    @Test
    @Transactional
    public void testDeleteProduct() {
        // First create a product
        String productJson = "{ \"name\": \"Product 1\", \"description\": \"Description of Product 1\", \"price\": 99.99, \"quantity\": 50 }";

        // Correct the type to Integer (or Long depending on your DB schema)
        Integer productId = given()
            .contentType(ContentType.JSON)
            .body(productJson)
            .when().post("/products")
            .then()
            .statusCode(201)
            .extract().path("id"); // Extract the id as Integer

        // Delete the product
        given()
            .when().delete("/products/" + productId)
            .then()
            .statusCode(204);

        // Verify the product is deleted
        given()
            .when().get("/products/" + productId)
            .then()
            .statusCode(404);
    }

    // Test for checking stock availability
    @Test
    public void testCheckStockAvailability() {
        // First create a product
        String productJson = "{ \"name\": \"Product 1\", \"description\": \"Description of Product 1\", \"price\": 99.99, \"quantity\": 50 }";
        Integer productId = given()
            .contentType(ContentType.JSON)
            .body(productJson)
            .when().post("/products")
            .then()
            .statusCode(201)
            .extract().path("id");

        // Check stock availability (should be true)
        given()
            .when().get("/products/" + productId + "/check-stock?count=30")
            .then()
            .statusCode(200)
            .body(equalTo("true"));

        // Check stock availability (should be false)
        given()
            .when().get("/products/" + productId + "/check-stock?count=60")
            .then()
            .statusCode(200)
            .body(equalTo("false"));
    }

    // Test for getting products sorted by price
    @Test
    public void testGetProductsSortedByPrice() {
        given()
            .when().get("/products/sorted-by-price")
            .then()
            .statusCode(200)
            .body("[0].price", lessThanOrEqualTo(50.0f))
            .body("[1].price", greaterThan(50.0f));
    }
}
