package com.example.demo;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

class ProductAcceptanceTest {

    @Test
    void productApiShouldBeAvailable() {

        RestAssured.baseURI =
                "http://demo-app:8080";

        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(200);
    }
}