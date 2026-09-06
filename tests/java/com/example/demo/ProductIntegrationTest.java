package com.example.demo;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductIntegrationTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void databaseIntegrationTest() {

        Product product =
                new Product("Keyboard", 50);

        Product saved =
                repository.save(product);

        Product result =
                repository.findById(saved.getId())
                          .orElse(null);

        assertNotNull(result);

        assertEquals(
                "Keyboard",
                result.getName()
        );
    }
}