package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.BrandDTO;
import com.diaperbazaar.project.entity.Brand;
import com.diaperbazaar.project.payload.ApiResponse;
import com.diaperbazaar.project.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/brands")
@CrossOrigin(origins = "*")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * Get all brands, optionally filtered by category
     * GET /api/brands?category={slug}
     */
    @GetMapping
    public ResponseEntity<List<BrandDTO>> getAllBrands(
            @RequestParam(required = false) String category
    ) {
        List<BrandDTO> brands = brandService.getAllBrands(category);
        return ResponseEntity.ok(brands);
    }

    /**
     * Get brand by ID
     * GET /api/brands/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BrandDTO> getBrandById(@PathVariable Long id) {
        BrandDTO brand = brandService.getBrandById(id);
        return ResponseEntity.ok(brand);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Brand>> create(@RequestBody Brand b) {
        return ResponseEntity.ok(new ApiResponse<>(true, "brand created", brandService.create(b)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Brand>> update(@PathVariable Long id, @RequestBody Brand b) {
        return ResponseEntity.ok(new ApiResponse<>(true, "brand updated", brandService.update(id, b)));
        }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        brandService.delete(id);
        return ResponseEntity.ok(new ApiResponse<>(true, "brand deleted", null));
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<ApiResponse<List<Brand>>> bulkUpload(@RequestParam("file") MultipartFile file) throws Exception {
        List<Brand> saved = brandService.bulkUpload(file);
        return ResponseEntity.ok(new ApiResponse<>(true, "brands uploaded", saved));
    }

}
