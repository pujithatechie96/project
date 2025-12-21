package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.BrandDTO;
import com.diaperbazaar.project.entity.Brand;
import com.diaperbazaar.project.entity.ProductSize;
import com.diaperbazaar.project.exception.ResourceNotFoundException;
import com.diaperbazaar.project.repository.ProductSizeRepository;
import com.diaperbazaar.project.util.ExcelHelper;
import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductSizeService {

    private final ProductSizeRepository repo;

    @Autowired
    public ProductSizeService(ProductSizeRepository repo) { this.repo = repo; }

    public ProductSize create(ProductSize ps) {
        return repo.save(ps);
    }


    public ProductSize update(Long id, ProductSize ps) {
        ProductSize ex = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("ProductSize not found"));
        ex.setSize(ps.getSize());
        ex.setStock(ps.getStock());
        ex.setSku(ps.getSku());
        return repo.save(ex);
    }

    public ProductSize getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("ProductSize not found"));
    }

    public List<ProductSize> getByProductId(Long id){
        return repo.findByProductId(id);
    }

    public void delete(Long id) {
        ProductSize ps = getById(id);
        repo.delete(ps);
    }

//    @Override
//    public Page<ProductSize> listAll(Long productId, Pageable pageable) {
//        if (productId == null) return repo.findAll(pageable);
//        List<ProductSize> list = repo.findByProductId(productId);
//        int start = (int) pageable.getOffset();
//        int end = Math.min(start + pageable.getPageSize(), list.size());
//        return new PageImpl<>(list.subList(start, end), pageable, list.size());
//    }

    public List<ProductSize> listAll() {
        return repo.findAll();
    }

    public List<ProductSize> bulkUpload(MultipartFile file) throws Exception {
        if (!ExcelHelper.hasExcelFormat(file)) throw new IllegalArgumentException("Not excel");
        List<ProductSize> sizes = ExcelHelper.parseProductSizes(file.getInputStream());
        return repo.saveAll(sizes);
    }
}

