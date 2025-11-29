package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.BrandDTO;
import com.diaperbazaar.project.entity.Brand;
import com.diaperbazaar.project.repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    public List<BrandDTO> getAllBrands(String categorySlug) {
        List<Brand> brands;
        if (categorySlug != null && !categorySlug.isEmpty()) {
            brands = brandRepository.findByCategorySlug(categorySlug);
        } else {
            brands = brandRepository.findAll();
        }
        return brands.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BrandDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found"));
        return convertToDTO(brand);
    }

    private BrandDTO convertToDTO(Brand brand) {
        BrandDTO dto = new BrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setLogo(brand.getLogo());
        dto.setDescription(brand.getDescription());
        return dto;
    }
}
