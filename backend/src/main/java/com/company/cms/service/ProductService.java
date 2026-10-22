package com.company.cms.service;

import com.company.cms.dto.ProductDtos.*;
import com.company.cms.entity.Product;
import com.company.cms.exception.ConflictException;
import com.company.cms.exception.ResourceNotFoundException;
import com.company.cms.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> getAllProducts(String category, Boolean activeOnly) {
        List<Product> list;
        if (category != null && !category.isBlank() && !category.equalsIgnoreCase("ALL")) {
            list = productRepository.findByCategory(category);
        } else if (Boolean.TRUE.equals(activeOnly)) {
            list = productRepository.findByActiveTrue();
        } else {
            list = productRepository.findAll();
        }
        return list.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToResponse(p);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new ConflictException("Product with SKU " + request.getSku() + " already exists.");
        }
        Product p = new Product();
        p.setSku(request.getSku());
        p.setName(request.getName());
        p.setCategory(request.getCategory());
        p.setUnitPrice(request.getUnitPrice());
        p.setUnitCost(request.getUnitCost());
        p.setActive(request.isActive());

        p = productRepository.save(p);
        return mapToResponse(p);
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (!p.getSku().equalsIgnoreCase(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new ConflictException("Product with SKU " + request.getSku() + " already exists.");
        }

        p.setSku(request.getSku());
        p.setName(request.getName());
        p.setCategory(request.getCategory());
        p.setUnitPrice(request.getUnitPrice());
        p.setUnitCost(request.getUnitCost());
        p.setActive(request.isActive());

        p = productRepository.save(p);
        return mapToResponse(p);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        productRepository.delete(p);
    }

    private ProductResponse mapToResponse(Product p) {
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getCategory(), p.getUnitPrice(), p.getUnitCost(), p.isActive());
    }
}
