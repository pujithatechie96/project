package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.entity.Category;
import com.diaperbazaar.project.payload.ApiResponse;
import com.diaperbazaar.project.repository.CategoryRepository;
import com.diaperbazaar.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@CrossOrigin(origins = "*")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return ResponseEntity.ok(category);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody Category category) {
        Category savedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(savedCategory);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody Category category) {
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        category.setId(existingCategory.getId());
        Category updatedCategory = categoryRepository.save(category);
        return ResponseEntity.ok(updatedCategory);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCategory(@PathVariable Long id) {
        categoryRepository.deleteById(id);
        return ResponseEntity.ok("Category deleted successfully");
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<ApiResponse<Category>> get(@PathVariable Long id) {
//        return ResponseEntity.ok(new ApiResponse<>(true, "category fetched", categoryService.getById(id)));
//    }
//
//    @PostMapping
//    public ResponseEntity<ApiResponse<Category>> create(@RequestBody Category c) {
//        return ResponseEntity.ok(new ApiResponse<>(true, "category created", categoryService.create(c)));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<ApiResponse<Category>> update(@PathVariable Long id, @RequestBody Category c) {
//        return ResponseEntity.ok(new ApiResponse<>(true, "category updated", categoryService.update(id, c)));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
//        categoryService.delete(id);
//        return ResponseEntity.ok(new ApiResponse<>(true, "category deleted", null));
//    }

    @PostMapping("/bulk-upload")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Category>>> bulkUpload(@RequestParam("file") MultipartFile file) throws Exception {
        List<Category> saved = categoryService.bulkUpload(file);
        return ResponseEntity.ok(new ApiResponse<>(true, "categories uploaded", saved));
    }
}