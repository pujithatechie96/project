package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.*;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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

    @Autowired
    StockService stockService;

    /* ================= SEARCH / LIST ================= */

    public Page<ProductResponseDTO> searchProducts(
            String keyword, List<String> category, List<Long> brandId,
            Double minPrice, Double maxPrice, String productSize, Pageable pageable
    ) {
        Page<Product> products = productRepository.searchProducts(
                keyword, category, brandId, productSize, minPrice, maxPrice, pageable
        );
        if (productSize != null) {
            return products.map(product -> mapToResponse(product, productSize));
        } else {
            return products.map(this::mapToResponse);
        }
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
        if (productSize != null) {
            return products.map(product -> mapToResponse(product, productSize));
        }
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

    public List<String> getAvailableSizes(List<String> categorySlug, List<String> brandName) {
        if ((brandName != null && !brandName.isEmpty()) && categorySlug != null && !categorySlug.isEmpty()) {
            return productRepository.findSizesByBrandsAndCategories(brandName, categorySlug);
        } else if (categorySlug != null && !categorySlug.isEmpty()) {
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
                .productType(product.getProductType())
                .rating(product.getRating())
                .reviewCount(product.getReviewCount())
                .build();
    }

    private ProductResponseDTO mapToResponse(Product product, String size) {

        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            throw new RuntimeException("Product has no variants: " + product.getId());
        }

        /* ---------- MAP VARIANTS ---------- */
        List<ProductVariantDTO> variantDTOs = product.getVariants().stream()
                .filter(v -> v.getStock() > 0) // always good
                .filter(v -> size == null || size.isBlank()
                        || size.equalsIgnoreCase(v.getSize()))
                .map(this::mapVariantToDto)
                .toList();

        /* ---------- DEFAULT VARIANT ---------- */
        ProductVariantDTO defaultVariant = variantDTOs.stream()
                .filter(ProductVariantDTO::getIsDefault)
                .findFirst()
                .orElse(variantDTOs.get(0));

        if (size != null && !size.isBlank()) {
            defaultVariant = variantDTOs.stream()
                    .filter(v -> size.equalsIgnoreCase(v.getSize()))
                    .findFirst()
                    .orElse(variantDTOs.get(0));
        }

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
                .sku(variant.getSku())
                .title(variant.getTitle())
                .description(variant.getDescription())

                .sellPrice(variant.getSellPrice())
                .offlineSellPrice(variant.getOfflineSellPrice())
                .buyPrice(variant.getBuyPrice())
                .originalPrice(variant.getOriginalPrice())
                .discountPercentage(variant.getDiscountPercentage())
                .gstPercentage(variant.getGstPercentage())

                .stock(variant.getStock())
                .inStock(variant.getStock() > 0)
                .isDefault(variant.getIsDefault())

                .images(images)
                .image(images.isEmpty() ? null : images.get(0))
                .features(features)
                .packCount(variant.getPackCount())
                .visibility(variant.getVisibility())

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
                .slug(brand.getSlug())
                .build();
    }

    private CategoryDTO mapCategory(Category category) {
        if (category == null) return null;

        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }


    public ProductResponseDTO createProduct(ProductCreateRequestDTO dto) {

//        if (productRepository.existsBySlug(dto.getSlug())) {
//            throw new RuntimeException("Slug already exists");
//        }

        Product product = new Product();
        mapDtoToEntity(dto, product);

        Product saved = productRepository.save(product);
        // If vendor/party is selected and createPurchaseRecord is true, create purchase records
        if (dto.getPartyId() != null && Boolean.TRUE.equals(dto.getCreatePurchaseRecord())) {
            createInitialPurchaseRecords(saved, dto.getPartyId());
        }

        // Reload to get updated stock values
        saved = productRepository.findById(saved.getId()).orElse(saved);

        return mapToResponse(saved);
    }


    private void createInitialPurchaseRecords(Product product, Long partyId) {
        if (product.getVariants() == null || product.getVariants().isEmpty()) {
            return;
        }

        for (ProductVariant variant : product.getVariants()) {
            if (variant.getStock() != null && variant.getStock() > 0) {
                PurchaseEntryDTO purchaseDTO = new PurchaseEntryDTO();
                purchaseDTO.setProductId(product.getId());
                purchaseDTO.setVariantId(variant.getId());
                purchaseDTO.setPartyId(partyId);
                purchaseDTO.setQuantity(variant.getStock());
                purchaseDTO.setUnitPrice(BigDecimal.valueOf(
                        variant.getBuyPrice() != null ? variant.getBuyPrice() : 0.0
                ));
                purchaseDTO.setNotes("Opening stock from product creation");

                try {
                    // This will create stock transaction, party transaction, and update balances
                    stockService.addPurchase(purchaseDTO, true);
                } catch (Exception e) {
                    // Log error but don't fail product creation
                    System.err.println("Failed to create purchase record for variant " +
                            variant.getId() + ": " + e.getMessage());
                }
            }
        }
    }

    @Transactional  // ADD THIS if missing
    public ProductResponseDTO updateProduct(Long id, ProductCreateRequestDTO dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // Clear old variants
        product.getVariants().clear();

        // Map new variants (your existing logic)
        dto.getVariants().forEach(variantDTO -> {
            ProductVariant variant = mapVariantDto(variantDTO, product);
            product.getVariants().add(variant);
        });

        // Save product (cascades inserts for new, but not reliable for updates)
        Product saved = productRepository.saveAndFlush(product);  // Flush forces SQL

        // CRITICAL: Re-fetch and explicitly save EACH variant to trigger SKU UPDATE
        for (ProductVariant variant : saved.getVariants()) {
            if (variant.getId() != null) {  // Existing
                ProductVariant freshVariant = productVariantRepository.findById(variant.getId())
                        .orElseThrow(() -> new RuntimeException("Variant not found: " + variant.getId()));
                freshVariant.setSku(dto.getVariants().stream()  // Re-apply from DTO
                        .filter(v -> v.getId() != null && v.getId().equals(freshVariant.getId()))
                        .findFirst().map(ProductVariantDTO::getSku).orElse(freshVariant.getSku()));
                // Set other fields similarly if needed
                productVariantRepository.saveAndFlush(freshVariant);  // Forces UPDATE SQL
            }
        }

        return mapToResponse(saved);
    }



    @Transactional
    public void deleteProduct(Long id) {
        productVariantRepository.deleteByProductId(id);
        productRepository.deleteById(id);
    }

    /* ================= ENTITY MAPPERS ================= */

    private void mapDtoToEntity(ProductCreateRequestDTO dto, Product product) {

        /* ---------- PRODUCT LEVEL ---------- */
        if (dto.getId() != null) product.setId(dto.getId());
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
        if (product.getVariants() != null) product.getVariants().clear();

        if (product.getVariants() == null) {
            product.setVariants(new ArrayList<>());
        }
        dto.getVariants().forEach(variantDTO -> {
            ProductVariant variant = mapVariantDto(variantDTO, product);
            product.getVariants().add(variant);
        });
    }


    private ProductVariant mapVariantDto(ProductVariantDTO dto, Product product) {
        ProductVariant v = null;
        if (dto.getId() != null) {
            v = productVariantRepository.findById(dto.getId()).get();
        } else {
            v = new ProductVariant();
        }
        v.setProduct(product);
        v.setSize(dto.getSize());
        v.setWearType(dto.getWearType());
        v.setVisibility(dto.getVisibility());
        v.setSellPrice(dto.getSellPrice());
        v.setOfflineSellPrice(dto.getOfflineSellPrice());
        v.setOriginalPrice(dto.getOriginalPrice());
        v.setBuyPrice(dto.getBuyPrice());
        v.setDiscountPercentage(dto.getDiscountPercentage());
        v.setGstPercentage(dto.getGstPercentage());
        v.setStock(dto.getStock());
        v.setVisibility(dto.getVisibility());
        v.setTitle(dto.getTitle());
        v.setPackCount(dto.getPackCount());
        v.setDescription(dto.getDescription());
        v.setPackCount(dto.getPackCount());
        v.setImages(dto.getImages() != null ? String.join(",", dto.getImages()) : null);
        v.setFeatures(dto.getFeatures() != null ? String.join(",", dto.getFeatures()) : null);
        v.setIsDefault(dto.getIsDefault());
        v.setSku(dto.getSku());

        return v;
    }

    /* ================= BULK UPLOAD ================= */

    public void saveBulkProducts(List<ProductCreateRequestDTO> products) {
        for (ProductCreateRequestDTO dto : products) {
            createProduct(dto);
        }
    }

    public ProductResponseDTO getProductByBarcode(String barcode) {
        ProductVariant variant = productVariantRepository.findBySku(barcode).get();
        if (variant == null) {
            throw new RuntimeException("Product not found for barcode: " + barcode);
        }
        ProductResponseDTO responseDTO =  mapToResponse(variant.getProduct());
        responseDTO.setDefaultVariant(mapVariantToDto(variant));
        return responseDTO;
    }
}