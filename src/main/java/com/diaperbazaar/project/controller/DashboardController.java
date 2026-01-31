package com.diaperbazaar.project.controller;


import com.diaperbazaar.project.entity.PurchaseOrder;
import com.diaperbazaar.project.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final EntityManager entityManager;

    @GetMapping("/sales-summary")
    public ResponseEntity<Map<String, BigDecimal>> getSalesSummary(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        String dateFilter = "";
        if (startDate != null && endDate != null) {
            dateFilter = " AND DATE(transaction_date) BETWEEN '" + startDate + "' AND '" + endDate + "'";
        }

        // Total Purchases with date filter
        BigDecimal totalPurchases = BigDecimal.ZERO;
        try {
            Object result = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(amount), 0) FROM party_transactions WHERE transaction_type = 'PURCHASE'" + dateFilter
            ).getSingleResult();
            totalPurchases = result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
        }

        String orderDateFilter = "";
        if (startDate != null && endDate != null) {
            orderDateFilter = " AND DATE(o.created_at) BETWEEN '" + startDate + "' AND '" + endDate + "'";
        }

        // Offline Sales with date filter
        BigDecimal totalOfflineSales = BigDecimal.ZERO;
        try {
            Object result = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o " +
                            "JOIN users u ON o.user_id = u.id " +
                            "WHERE u.role = 'ADMIN'" + orderDateFilter
            ).getSingleResult();
            totalOfflineSales = result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Online Sales with date filter
        BigDecimal totalOnlineSales = BigDecimal.ZERO;
        try {
            Object result = entityManager.createNativeQuery(
                    "SELECT COALESCE(SUM(o.total_amount), 0) FROM orders o " +
                            "JOIN users u ON o.user_id = u.id " +
                            "WHERE u.role = 'USER'" + orderDateFilter
            ).getSingleResult();
            totalOnlineSales = result != null ? new BigDecimal(result.toString()) : BigDecimal.ZERO;
        } catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, BigDecimal> summary = new HashMap<>();
        summary.put("totalPurchases", totalPurchases);
        summary.put("totalOfflineSales", totalOfflineSales);
        summary.put("totalOnlineSales", totalOnlineSales);
        summary.put("totalSales", totalOfflineSales.add(totalOnlineSales));

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            // Total orders
            Query orderCountQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM orders");
            stats.put("totalOrders", ((Number) orderCountQuery.getSingleResult()).longValue());

            // Total revenue
            Query revenueQuery = entityManager.createNativeQuery("SELECT COALESCE(SUM(total_amount), 0) FROM orders");
            stats.put("totalRevenue", new BigDecimal(revenueQuery.getSingleResult().toString()));

            // Total products
            Query productCountQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM products");
            stats.put("totalProducts", ((Number) productCountQuery.getSingleResult()).longValue());

            // Total users
            Query userCountQuery = entityManager.createNativeQuery("SELECT COUNT(*) FROM users");
            stats.put("totalUsers", ((Number) userCountQuery.getSingleResult()).longValue());

            // Recent orders (last 5)
            Query recentOrdersQuery = entityManager.createNativeQuery(
                    "SELECT o.id, o.status, o.total_amount, o.created_at FROM orders o ORDER BY o.created_at DESC LIMIT 5"
            );
            List<Object[]> recentOrders = recentOrdersQuery.getResultList();
            stats.put("recentOrders", recentOrders.stream().map(row -> {
                Map<String, Object> order = new HashMap<>();
                order.put("id", row[0]);
                order.put("status", row[1]);
                order.put("totalAmount", row[2]);
                order.put("createdAt", row[3]);
                return order;
            }).toList());

        } catch (Exception e) {
            stats.put("totalOrders", 0);
            stats.put("totalRevenue", BigDecimal.ZERO);
            stats.put("totalProducts", 0);
            stats.put("totalUsers", 0);
            stats.put("recentOrders", List.of());
        }

        return ResponseEntity.ok(stats);
    }
}
