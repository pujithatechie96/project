package com.diaperbazaar.project.repository;


import com.diaperbazaar.project.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Order> findByStatusOrderByCreatedAtDesc(Order.OrderStatus status);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.userId = :userId ORDER BY o.createdAt DESC")
    List<Order> findByUserIdWithItems(Long userId);

    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Order findByIdWithItems(Long orderId);
}