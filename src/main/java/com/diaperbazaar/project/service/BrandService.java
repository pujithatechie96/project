package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.BrandDTO;
import com.diaperbazaar.project.entity.Brand;
import com.diaperbazaar.project.exception.ResourceNotFoundException;
import com.diaperbazaar.project.repository.BrandRepository;
import com.diaperbazaar.project.util.ExcelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
        if (brand == null) return null;

        return BrandDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .logo(brand.getLogo())
                .build();
    }



    public Brand create(Brand brand) {
        return brandRepository.save(brand);
    }


    public Brand update(Long id, Brand brand) {
        Brand existing = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id " + id));
        existing.setName(brand.getName());
        existing.setLogo(brand.getLogo());
        existing.setDescription(brand.getDescription());
        existing.setSlug(brand.getSlug());
        return brandRepository.save(existing);
    }

    public Brand getById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id " + id));
    }

    public void delete(Long id) {
        Brand b = getById(id);
        brandRepository.delete(b);
    }

//    @Override
//    public Page<Brand> listAll(String q, Pageable pageable) {
//        if (q == null || q.trim().isEmpty()) {
//            return brandRepository.findAll(pageable);
//        } else {
//            Brand probe = new Brand();
//            probe.setName(q);
//            ExampleMatcher matcher = ExampleMatcher.matchingAny()
//                    .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
//            Example<Brand> example = Example.of(probe, matcher);
//            return brandRepository.findAll(example, pageable);
//        }
//    }

    public List<Brand> bulkUpload(MultipartFile file) throws Exception {
        if (!ExcelHelper.hasExcelFormat(file)) {
            throw new IllegalArgumentException("File is not excel type");
        }
        List<Brand> brands = ExcelHelper.parseBrands(file.getInputStream());
        return brandRepository.saveAll(brands);
    }
}

