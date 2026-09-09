package com.example.demo;

import org.junit.jupiter.api.Test;

import io.restassured.RestAssured;

import static io.restassured.RestAssured.*;

class ProductAcceptanceTest {

    @Test
    void productApiShouldBeAvailable() {

        String baseUrl =
                System.getProperty("baseUrl", "http://localhost:8081");

        RestAssured.baseURI = baseUrl;

        given()
        .when()
            .get("/api/products")
        .then()
            .statusCode(200);
    }
}