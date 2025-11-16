package com.batuhan.emg_service_product.controller;

import com.batuhan.emg_service_product.dto.ProductCreateRequest;
import com.batuhan.emg_service_product.dto.ProductResponse;
import com.batuhan.emg_service_product.dto.StockUpdateRequest;
import com.batuhan.emg_service_product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @PreAuthorize("hasAuthority('product:create')")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        ProductResponse response = productService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('product:read')")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> response = productService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('product:update')")
    public ResponseEntity<ProductResponse> updateStock(@PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        ProductResponse response = productService.updateStock(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('product:delete')")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}