package br.com.fiap.vaultix.bdd;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;

public class RestAssuredConfig {

    public static final String BASE_URL = System.getProperty("api.base.url", "http://localhost:8080");

    private static RequestSpecification requestSpec;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URL;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilter(new RequestLoggingFilter())
                .addFilter(new ResponseLoggingFilter())
                .build();
    }

    public static RequestSpecification getRequestSpec() {
        if (requestSpec == null) {
            setup();
        }
        return requestSpec;
    }
}
