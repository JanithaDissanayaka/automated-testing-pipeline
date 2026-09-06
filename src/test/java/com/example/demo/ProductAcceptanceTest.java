package com.example.demo;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.*;

class ProductAcceptanceTest {

    @Test
    void productApiShouldBeAvailable() {

        RestAssured.baseURI =
                "http://localhost:8081";

        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(200);
    }
}