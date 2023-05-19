package de.hsaalen.anwendungssicherheit;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class KontoResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
          .when()
                .header("Content-Type", "application/json")
                .body("{ \"beschreibung\": \"Test\" }").post("/konto");
          //.then()
             //.statusCode(200)
             //.body(is("Hello RESTEasy"));
    }

}