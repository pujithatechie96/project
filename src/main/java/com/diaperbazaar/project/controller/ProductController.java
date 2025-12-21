package com.diaperbazaar.project.controller;



import com.diaperbazaar.project.dto.ProductCreateRequestDTO;
import com.diaperbazaar.project.dto.ProductDTO;
import com.diaperbazaar.project.dto.ProductResponseDTO;
import com.diaperbazaar.project.service.ExcelService;
import com.diaperbazaar.project.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ExcelService excelService;

    /**
     * Search products with filters
     * GET /api/products/search
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponseDTO>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<com.diaperbazaar.project.dto.ProductResponseDTO> products = productService.searchProducts(
                keyword, category, brandId, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(products);
    }

    /**
     * Get all products with optional filters
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String wearType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.getAllProducts(
                category, brandId, productSize, productType, wearType, minPrice, maxPrice, sortBy, pageable
        );
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by category slug
     * GET /api/products/category/{slug}
     */
    @GetMapping("/category/{slug}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByCategory(
            @PathVariable String slug,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) String productType,
            @RequestParam(required = false) String wearType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.getProductsByCategory(
                slug, brandId, productSize,productType,wearType, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(products);
    }

    /**
     * Get products by brand
     * GET /api/products/brand/{brandId}
     */
    @GetMapping("/brand/{brandId}")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsByBrand(
            @PathVariable Long brandId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.getProductsByBrand(
                brandId, category, minPrice, maxPrice, pageable
        );
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Get product by slug
     * GET /api/products/slug/{slug}
     */
    @GetMapping("/slug/{slug}")
    public ResponseEntity<ProductResponseDTO> getProductBySlug(@PathVariable String slug) {
        ProductResponseDTO product = productService.getProductBySlug(slug);
        return ResponseEntity.ok(product);
    }

    /**
     * Get available sizes, optionally filtered by category
     * GET /api/products/sizes?category={slug}
     */
    @GetMapping("/sizes")
    public ResponseEntity<List<String>> getAvailableSizes(
            @RequestParam(required = false) String category
    ) {
        List<String> sizes = productService.getAvailableSizes(category);
        return ResponseEntity.ok(sizes);
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @RequestBody ProductCreateRequestDTO dto
    ) {
        return ResponseEntity.ok(productService.createProduct(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductCreateRequestDTO dto
    ) {
        return ResponseEntity.ok(productService.updateProduct(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok("Product deleted successfully");
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadProducts(@RequestParam("file") MultipartFile file) {
        try {
            List<ProductCreateRequestDTO> products = excelService.parseExcel(file);
            productService.saveBulkProducts(products);
            return ResponseEntity.ok("Bulk upload successful");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Error: " + e.getMessage());
        }
    }




}