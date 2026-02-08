package com.diaperbazaar.project.repository;

import com.diaperbazaar.project.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Optional<Customer> findByMobile(String mobile);
    boolean existsByMobile(String mobile);
    @Query("SELECT c FROM Customer c WHERE c.mobile LIKE %:search% OR c.name LIKE %:search%")
    List<Customer> searchByMobileOrName(@Param("search") String search);
    @Query("SELECT c FROM Customer c ORDER BY c.totalPoints DESC")
    List<Customer> findAllOrderByPointsDesc();
}
