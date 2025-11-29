package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    @Query(value = "SELECT DISTINCT b.* FROM brands b JOIN products p ON b.id = p.brand_id JOIN categories c ON p.category_id = c.id WHERE c.slug =:categorySlug ", nativeQuery = true)
    List<Brand> findByCategorySlug(@Param("categorySlug") String categorySlug);
}

