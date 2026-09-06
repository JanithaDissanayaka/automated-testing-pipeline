package com.example.demo;

import com.example.demo.model.Product;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductRegressionTest {

    @Test
    void productNameShouldNotBeNull() {

        Product product =
                new Product("Mouse", 25);

        assertNotNull(product.getName());
    }

    @Test
    void productPriceShouldBeCorrect() {

        Product product =
                new Product("Monitor", 300);

        assertEquals(
                300,
                product.getPrice()
        );
    }

    @Test
    void productNameCanBeUpdated() {

        Product product =
                new Product("Laptop", 1000);

        product.setName("Gaming Laptop");

        assertEquals(
                "Gaming Laptop",
                product.getName()
        );
    }
}