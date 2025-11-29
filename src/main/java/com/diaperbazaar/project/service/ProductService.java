package com.diaperbazaar.project.service;


import com.diaperbazaar.project.dto.ProductResponseDTO;
import com.diaperbazaar.project.dto.ProductSizeDTO;
import com.diaperbazaar.project.entity.Product;
import com.diaperbazaar.project.entity.ProductSize;
import com.diaperbazaar.project.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public Page<ProductResponseDTO> searchProducts(
            String keyword, String category, Long brandId,
            Double minPrice, Double maxPrice, Pageable pageable
    ) {
        Page<Product> products = productRepository.searchProducts(
                keyword, category, brandId, minPrice, maxPrice, pageable
        );
        return products.map(this::convertToDTO);
    }

    public Page<ProductResponseDTO> getAllProducts(
            String category, Long brandId, Double minPrice, Double maxPrice,
            String sortBy, Pageable pageable
    ) {
        if (category != null) {
            return getProductsByCategory(category, brandId, minPrice, maxPrice, pageable);
        }
        if (brandId != null) {
            return getProductsByBrand(brandId, category, minPrice, maxPrice, pageable);
        }

        Page<Product> products = productRepository.findAllWithFilters(
                minPrice, maxPrice, pageable
        );
        return products.map(this::convertToDTO);
    }

    public Page<ProductResponseDTO> getProductsByCategory(
            String categorySlug, Long brandId, Double minPrice, Double maxPrice,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.findByCategorySlugWithFilters(
                categorySlug, brandId, minPrice, maxPrice, pageable
        );
        return products.map(this::convertToDTO);
    }

    public Page<ProductResponseDTO> getProductsByBrand(
            Long brandId, String category, Double minPrice, Double maxPrice,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.findByBrandWithFilters(
                brandId, category, minPrice, maxPrice, pageable
        );
        return products.map(this::convertToDTO);
    }

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDTO(product);
    }

    public ProductResponseDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return convertToDTO(product);
    }

    public List<String> getAvailableSizes(String categorySlug) {
        if (categorySlug != null && !categorySlug.isEmpty()) {
            return productRepository.findDistinctSizesByCategorySlug(categorySlug);
        }
        return productRepository.findDistinctSizes();
    }


    private ProductResponseDTO convertToDTO(Product product) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setSlug(product.getSlug());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setRating(product.getRating());
        dto.setReviewCount(product.getReviewCount());
        dto.setInStock(product.isInStock());
        dto.setSku(product.getSku());
        dto.setCreatedAt(product.getCreatedAt());

        // Set brand info
        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getName());
        }

        // Set category info
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
            dto.setCategorySlug(product.getCategory().getSlug());
        }

        // Parse images JSON string to array
        if (product.getImages() != null && !product.getImages().isEmpty()) {
            String[] imageArray = product.getImages()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .split(",");
            dto.setImages(Arrays.stream(imageArray)
                    .map(String::trim)
                    .collect(Collectors.toList()));
            dto.setImage(imageArray.length > 0 ? imageArray[0].trim() : "");
        }

        // Parse features JSON string to array
        if (product.getFeatures() != null && !product.getFeatures().isEmpty()) {
            String[] featureArray = product.getFeatures()
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .split(",");
            dto.setFeatures(Arrays.stream(featureArray)
                    .map(String::trim)
                    .collect(Collectors.toList()));
        }

        // Convert sizes
        if (product.getSizes() != null) {
            List<ProductSizeDTO> sizeDTOs = product.getSizes().stream()
                    .map(this::convertSizeToDTO)
                    .collect(Collectors.toList());
            dto.setSizes(sizeDTOs);
        }

        return dto;
    }

    private ProductSizeDTO convertSizeToDTO(ProductSize size) {
        ProductSizeDTO dto = new ProductSizeDTO();
        dto.setId(size.getId());
        dto.setSize(size.getSize());
        dto.setStock(size.getStock());
        dto.setPrice(size.getPrice());
        dto.setSku(size.getSku());
        return dto;
    }
}