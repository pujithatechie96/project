package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, Long> {
    
    List<Vendor> findAllByOrderByNameAsc();
    
    List<Vendor> findByIsActiveTrue();
    
    @Query("SELECT v FROM Vendor v WHERE " +
           "LOWER(v.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(v.companyName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "v.phone LIKE CONCAT('%', :keyword, '%') OR " +
           "LOWER(v.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Vendor> searchByKeyword(@Param("keyword") String keyword);
}
