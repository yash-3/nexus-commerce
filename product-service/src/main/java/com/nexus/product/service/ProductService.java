package com.nexus.product.service;

import com.nexus.product.dto.PriceCheckResponse;
import com.nexus.product.dto.ProductCreateRequest;
import com.nexus.product.dto.ProductResponse;
import com.nexus.product.dto.ProductUpdateRequest;
import com.nexus.product.entity.Product;
import com.nexus.product.exception.DuplicateProductException;
import com.nexus.product.exception.ProductNotFoundException;
import com.nexus.product.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.sku())) {
            throw new DuplicateProductException("Product with SKU '" + request.sku() + "' already exists");
        }

        Product product = new Product(
                request.sku(),
                request.name(),
                request.description(),
                request.price(),
                request.category(),
                true
        );

        Product saved = productRepository.save(product);
        return mapToResponse(saved);
    }

    public ProductResponse getProductById(Long id) {
        return productRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));
    }

    public ProductResponse getProductBySku(String sku) {
        return productRepository.findBySku(sku)
                .map(this::mapToResponse)
                .orElseThrow(() -> new ProductNotFoundException("Product with SKU '" + sku + "' not found"));
    }

    public List<ProductResponse> getAllProducts(Boolean activeOnly, String category) {
        List<Product> products;
        boolean onlyActive = Boolean.TRUE.equals(activeOnly);

        if (category != null && !category.isBlank()) {
            products = onlyActive
                    ? productRepository.findByCategoryIgnoreCaseAndActiveTrue(category.trim())
                    : productRepository.findByCategoryIgnoreCase(category.trim());
        } else {
            products = onlyActive
                    ? productRepository.findByActiveTrue()
                    : productRepository.findAll();
        }

        return products.stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product with ID " + id + " not found"));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setActive(request.active());

        Product updated = productRepository.save(product);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException("Product with ID " + id + " not found");
        }
        productRepository.deleteById(id);
    }

    public PriceCheckResponse checkPrice(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("Product with SKU '" + sku + "' not found"));

        return new PriceCheckResponse(
                product.getSku(),
                product.getPrice(),
                product.isActive()
        );
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.isActive(),
                product.getCreatedAt(),
                product.getUpdatedAt()
        );
    }
}
