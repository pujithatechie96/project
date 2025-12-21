package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.*;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    /* ================= SEARCH / LIST ================= */

    public Page<ProductResponseDTO> searchProducts(
            String keyword, String category, Long brandId,
            Double minPrice, Double maxPrice, Pageable pageable
    ) {
        Page<Product> products = productRepository.searchProducts(
                keyword, category, brandId, minPrice, maxPrice, pageable
        );
        return products.map(this::mapToResponse);
    }

    public Page<ProductResponseDTO> getAllProducts(
            String category, Long brandId, String productSize,
            String productType, String wearType, Double minPrice, Double maxPrice,
            String sortBy, Pageable pageable
    ) {
        if (category != null) {
            return getProductsByCategory(category, brandId, productSize, productType, wearType, minPrice, maxPrice, pageable);
        }

        Page<Product> products = productRepository.findAllWithFilters(
                minPrice, maxPrice, pageable
        );

        return products.map(this::mapToResponse);
    }

    public Page<ProductResponseDTO> getProductsByCategory(
            String categorySlug, Long brandId, String productSize,
            String productType, String wearType, Double minPrice, Double maxPrice,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.findByCategorySlugWithFilters(
                categorySlug, brandId, productSize, productType, wearType, minPrice, maxPrice, pageable
        );
        return products.map(this::mapToResponse);
    }

    public Page<ProductResponseDTO> getProductsByBrand(
            Long brandId, String category, Double minPrice, Double maxPrice,
            Pageable pageable
    ) {
        Page<Product> products = productRepository.findByBrandWithFilters(
                brandId, category, minPrice, maxPrice, pageable
        );
        return products.map(p -> mapToResponse(p));
    }

    /* ================= SINGLE PRODUCT ================= */

    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return mapToResponse(product);
    }

    public ProductResponseDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug);
        return mapToResponse(product);
    }

    /* ================= AVAILABLE SIZES ================= */

    public List<String> getAvailableSizes(String categorySlug) {
        if (categorySlug != null && !categorySlug.isEmpty()) {
            return productRepository.findDistinctSizesByCategorySlug(categorySlug);
        }
        return productRepository.findDistinctSizes();
    }

    /* ================= CORE RESPONSE MAPPER ================= */
    private ProductResponseDTO mapToResponse(Product product) {

        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            throw new RuntimeException("Product has no variants: " + product.getId());
        }

        /* ---------- MAP VARIANTS ---------- */
        List<ProductVariantDTO> variantDTOs = product.getVariants().stream()
                .map(this::mapVariantToDto)
                .toList();

        /* ---------- DEFAULT VARIANT ---------- */
        ProductVariantDTO defaultVariant = variantDTOs.stream()
                .filter(ProductVariantDTO::getIsDefault)
                .findFirst()
                .orElse(variantDTOs.get(0));

        return ProductResponseDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .brand(mapBrand(product.getBrand()))
                .category(mapCategory(product.getCategory()))
                .defaultVariant(defaultVariant)
                .variants(variantDTOs)
                .build();
    }



    /* ================= CREATE / UPDATE ================= */


    private ProductVariantDTO mapVariantToDto(ProductVariant variant) {

        List<String> images = parseCsv(variant.getImages());
        List<String> features = parseCsv(variant.getFeatures());

        return ProductVariantDTO.builder()
                .id(variant.getId())
                .size(variant.getSize())
                .wearType(variant.getWearType())

                .title(variant.getTitle())
                .description(variant.getDescription())

                .sellPrice(variant.getSellPrice())
                .originalPrice(variant.getOriginalPrice())
                .discountPercentage(variant.getDiscountPercentage())

                .stock(variant.getStock())
                .inStock(variant.getStock() > 0)
                .isDefault(variant.getIsDefault())

                .images(images)
                .image(images.isEmpty() ? null : images.get(0))
                .features(features)

                .build();
    }
    private List<String> parseCsv(String value) {
        if (value == null || value.isBlank()) return List.of();

        return Arrays.stream(value.replace("[", "")
                        .replace("]", "")
                        .replace("\"", "")
                        .split(","))
                .map(String::trim)
                .toList();
    }

    private BrandDTO mapBrand(Brand brand) {
        if (brand == null) return null;

        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .build();
    }

    private CategoryDTO mapCategory(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }



    public ProductResponseDTO createProduct(ProductCreateRequestDTO dto) {

        if (productRepository.existsBySlug(dto.getSlug())) {
            throw new RuntimeException("Slug already exists");
        }

        Product product = new Product();
        mapDtoToEntity(dto, product);

        Product saved = productRepository.save(product);

        /* -------- SAVE VARIANTS -------- */
        if (dto.getVariants() != null) {
            for (ProductVariantDTO v : dto.getVariants()) {
                ProductVariant variant = mapVariantDto(v, saved);
                productVariantRepository.save(variant);
            }
        }

        return mapToResponse(saved);
    }

    public ProductResponseDTO updateProduct(Long id, ProductCreateRequestDTO dto) {

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        mapDtoToEntity(dto, product);
        Product saved = productRepository.save(product);

        productVariantRepository.deleteByProductId(saved.getId());

        if (dto.getVariants() != null) {
            for (ProductVariantDTO v : dto.getVariants()) {
                productVariantRepository.save(mapVariantDto(v, saved));
            }
        }

        return mapToResponse(saved);
    }

    public void deleteProduct(Long id) {
        productVariantRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    /* ================= ENTITY MAPPERS ================= */

    private void mapDtoToEntity(ProductCreateRequestDTO dto, Product product) {

        /* ---------- PRODUCT LEVEL ---------- */
        product.setName(dto.getName());
        product.setSlug(dto.getSlug());
        product.setProductType(
                dto.getProductType() != null ? dto.getProductType() : "regular"
        );
        product.setRating(dto.getRating());
        product.setReviewCount(dto.getReviewCount());

        /* ---------- BRAND ---------- */
        Brand brand = brandRepository.findById(dto.getBrandId())
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        product.setBrand(brand);

        /* ---------- CATEGORY ---------- */
        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        product.setCategory(category);

        /* ---------- VARIANTS ---------- */
        product.getVariants().clear();

        dto.getVariants().forEach(variantDTO -> {
            ProductVariant variant = mapVariantDto(variantDTO, product);
            product.getVariants().add(variant);
        });
    }


    private ProductVariant mapVariantDto(ProductVariantDTO dto, Product product) {

        ProductVariant v = new ProductVariant();
        v.setProduct(product);
        v.setSize(dto.getSize());
        v.setWearType(dto.getWearType());
        v.setSellPrice(dto.getSellPrice());
        v.setOriginalPrice(dto.getOriginalPrice());
        v.setDiscountPercentage(dto.getDiscountPercentage());
        v.setStock(dto.getStock());
        v.setTitle(dto.getTitle());
        v.setDescription(dto.getDescription());
        v.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        v.setFeatures(dto.getFeatures() != null ? String.join(",", dto.getFeatures()) : null);
        v.setIsDefault(dto.getIsDefault());

        return v;
    }

    /* ================= BULK UPLOAD ================= */

    public void saveBulkProducts(List<ProductCreateRequestDTO> products) {
        for (ProductCreateRequestDTO dto : products) {
            createProduct(dto);
        }
    }
}
