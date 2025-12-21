package com.diaperbazaar.project.service;

import com.diaperbazaar.project.entity.Category;
import com.diaperbazaar.project.exception.ResourceNotFoundException;
import com.diaperbazaar.project.repository.CategoryRepository;
import com.diaperbazaar.project.util.ExcelHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository repo;

    @Autowired
    public CategoryService(CategoryRepository repo) { this.repo = repo; }

    public Category create(Category category) {
        return repo.save(category);
    }

    public Category update(Long id, Category category) {
        Category ex = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        ex.setName(category.getName());
        ex.setSlug(category.getSlug());
        ex.setDescription(category.getDescription());
//        ex.setImage(category.getImage());
        return repo.save(ex);
    }

    public Category getById(Long id) {
        return repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Category not found"));
    }

    public void delete(Long id) {
        Category c = getById(id);
        repo.delete(c);
    }

//    @Override
//    public Page<Category> listAll(String q, Pageable pageable) {
//        if (q == null || q.trim().isEmpty()) return repo.findAll(pageable);
//        Category probe = new Category();
//        probe.setName(q);
//        ExampleMatcher matcher = ExampleMatcher.matchingAny()
//                .withMatcher("name", ExampleMatcher.GenericPropertyMatchers.contains().ignoreCase());
//        Example<Category> example = Example.of(probe, matcher);
//        return repo.findAll(example, pageable);
//    }

    public List<Category> bulkUpload(MultipartFile file) throws Exception {
        if (!ExcelHelper.hasExcelFormat(file)) throw new IllegalArgumentException("Not excel");
        List<Category> list = ExcelHelper.parseCategories(file.getInputStream());
        return repo.saveAll(list);
    }

}
