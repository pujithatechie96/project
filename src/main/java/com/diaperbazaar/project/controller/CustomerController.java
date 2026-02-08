package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.*;
import com.diaperbazaar.project.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final CustomerService customerService;
    /**
     * Get or create customer by mobile number
     * Used when placing an order - creates new customer if doesn't exist
     */
    @PostMapping("/get-or-create")
    public ResponseEntity<CustomerDTO> getOrCreateCustomer(@RequestBody CreateCustomerRequest request) {
        if (request.getMobile() == null || request.getMobile().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        CustomerDTO customer = customerService.getOrCreateCustomer(
                request.getName().trim(),
                request.getMobile().trim()
        );
        return ResponseEntity.ok(customer);
    }
    /**
     * Search customer by mobile number
     * Returns customer with current points balance
     */
    @GetMapping("/search/mobile/{mobile}")
    public ResponseEntity<CustomerDTO> searchByMobile(@PathVariable String mobile) {
        CustomerDTO customer = customerService.getCustomerByMobile(mobile);
        if (customer == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(customer);
    }
    /**
     * Search customers by mobile or name (partial match)
     */
    @GetMapping("/search")
    public ResponseEntity<List<CustomerDTO>> searchCustomers(@RequestParam String query) {
        List<CustomerDTO> customers = customerService.searchCustomers(query);
        return ResponseEntity.ok(customers);
    }
    /**
     * Get all customers ordered by points (highest first)
     */
    @GetMapping
    public ResponseEntity<List<CustomerDTO>> getAllCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }
    /**
     * Get customer by ID
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<CustomerDTO> getCustomerById(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerById(customerId));
    }
    /**
     * Credit points to customer after order completion
     * Called after order is placed successfully
     */
    @PostMapping("/{customerId}/credit-points")
    public ResponseEntity<CustomerDTO> creditPoints(
            @PathVariable Long customerId,
            @RequestParam Long orderId,
            @RequestParam BigDecimal orderTotal) {
        CustomerDTO customer = customerService.creditPointsForOrder(customerId, orderId, orderTotal);
        return ResponseEntity.ok(customer);
    }
    /**
     * Redeem points for discount
     * Returns the discount amount (10 points = ₹1)
     */
    @PostMapping("/redeem-points")
    public ResponseEntity<RedeemPointsResponse> redeemPoints(@RequestBody RedeemPointsRequest request) {
        if (request.getCustomerId() == null || request.getPointsToRedeem() == null) {
            return ResponseEntity.badRequest().build();
        }
        RedeemPointsResponse response = customerService.redeemPoints(
                request.getCustomerId(),
                request.getPointsToRedeem(),
                request.getOrderId()
        );
        return ResponseEntity.ok(response);
    }
    /**
     * Get customer points transaction history
     */
    @GetMapping("/{customerId}/points-history")
    public ResponseEntity<List<CustomerPointsLedgerDTO>> getPointsHistory(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getCustomerPointsHistory(customerId));
    }
    /**
     * Get maximum redeemable amount for customer
     */
    @GetMapping("/{customerId}/max-redeemable")
    public ResponseEntity<BigDecimal> getMaxRedeemable(@PathVariable Long customerId) {
        return ResponseEntity.ok(customerService.getMaxRedeemableAmount(customerId));
    }

    /**
     * Get all orders for a customer by mobile number
     * Returns complete customer details, all orders with items, points history
     */
    @GetMapping("/orders/by-mobile/{mobile}")
    public ResponseEntity<CustomerOrdersResponse> getCustomerOrdersByMobile(@PathVariable String mobile) {
        try {
            CustomerOrdersResponse response = customerService.getCustomerOrdersByMobile(mobile);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get all orders for a customer by customer ID
     * Returns complete customer details, all orders with items, points history
     */
    @GetMapping("/{customerId}/orders")
    public ResponseEntity<CustomerOrdersResponse> getCustomerOrdersById(@PathVariable Long customerId) {
        try {
            CustomerOrdersResponse response = customerService.getCustomerOrdersById(customerId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}