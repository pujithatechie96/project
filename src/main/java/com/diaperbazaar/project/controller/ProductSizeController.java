package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.entity.ProductSize;
import com.diaperbazaar.project.payload.ApiResponse;
import com.diaperbazaar.project.service.ProductSizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/product-sizes")
public class ProductSizeController {
    private final ProductSizeService service;

    @Autowired
    public ProductSizeController(ProductSizeService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSize>>> list(@RequestParam(value = "productId", required = false) Long productId) {
        List<ProductSize> p = new ArrayList<>();
        if(productId!=null){
             p = service.getByProductId(productId);
        }else{
             p = service.listAll();
        }
        return ResponseEntity.ok(new ApiResponse<>(true, "product sizes fetched", p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSize>> get(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "product size fetched", service.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductSize>> create(@RequestBody ProductSize s) {
        return ResponseEntity.ok(new ApiResponse<>(true, "product size created", service.create(s)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductSize>> update(@PathVariable Long id, @RequestBody ProductSize s) {
        return ResponseEntity.ok(new ApiResponse<>(true, "product size updated", service.update(id, s)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "product size deleted", null));
    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductSize>>> bulkUpload(@RequestParam("file") MultipartFile file) throws Exception {
        List<ProductSize> saved = service.bulkUpload(file);
        return ResponseEntity.ok(new ApiResponse<>(true, "product sizes uploaded", saved));
    }
}
