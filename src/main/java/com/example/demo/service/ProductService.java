package com.example.demo.service;

import com.example.demo.model.Product;
import com.example.demo.repository.ProductRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository repository;

    public ProductService(ProductRepository repository) {
        this.repository = repository;
    }

    public List<Product> getAllProducts() {
        return repository.findAll();
    }

    public Optional<Product> getProduct(Long id) {
        return repository.findById(id);
    }

    public Product createProduct(Product product) {
        return repository.save(product);
    }

    public void deleteProduct(Long id) {
        repository.deleteById(id);
    }
}