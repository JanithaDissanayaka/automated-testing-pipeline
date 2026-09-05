package com.example.demo;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ProductFunctionalTest {

    @Autowired
    private ProductRepository repository;

    @Test
    void applicationContextLoads() {

        assertNotNull(repository);
    }

    @Test
    void createProductTest() {

        Product product =
                new Product("Laptop", 1500);

        Product saved =
                repository.save(product);

        assertNotNull(saved.getId());
        assertEquals("Laptop", saved.getName());
        assertEquals(1500, saved.getPrice());
    }
}