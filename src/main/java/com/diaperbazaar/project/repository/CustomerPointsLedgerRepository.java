package com.diaperbazaar.project.repository;
import com.diaperbazaar.project.entity.CustomerPointsLedger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface CustomerPointsLedgerRepository extends JpaRepository<CustomerPointsLedger, Long> {
    List<CustomerPointsLedger> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
    List<CustomerPointsLedger> findByOrderId(Long orderId);
    @Query("SELECT SUM(CASE WHEN cpl.transactionType = 'CREDIT' THEN cpl.points ELSE -cpl.points END) " +
            "FROM CustomerPointsLedger cpl WHERE cpl.customer.id = :customerId")
    Integer calculateTotalPointsByCustomerId(@Param("customerId") Long customerId);
}
