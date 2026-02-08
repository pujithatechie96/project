package com.diaperbazaar.project.service;
import com.diaperbazaar.project.dto.*;
import com.diaperbazaar.project.entity.Customer;
import com.diaperbazaar.project.entity.CustomerPointsLedger;
import com.diaperbazaar.project.entity.Order;
import com.diaperbazaar.project.entity.OrderItem;
import com.diaperbazaar.project.repository.CustomerPointsLedgerRepository;
import com.diaperbazaar.project.repository.CustomerRepository;
import com.diaperbazaar.project.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CustomerPointsLedgerRepository pointsLedgerRepository;
    private final OrderRepository orderRepository;

    // Points configuration: 10% of order total as points, 10 points = 1 rupee
    private static final BigDecimal POINTS_EARN_PERCENTAGE = new BigDecimal("0.10"); // 10%
    private static final int POINTS_PER_RUPEE = 10;
    /**
     * Get or create customer by mobile number
     */
    @Transactional
    public CustomerDTO getOrCreateCustomer(String name, String mobile) {
        return CustomerDTO.fromEntity(getOrCreateCustomers(name, mobile));
    }

    @Transactional
    public Customer getOrCreateCustomers(String name, String mobile) {
        Customer customer = customerRepository.findByMobile(mobile)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setName(name);
                    newCustomer.setMobile(mobile);
                    newCustomer.setTotalPoints(0);
                    return customerRepository.save(newCustomer);
                });
        // Update name if different (customer might give different name)
        if (!customer.getName().equals(name)) {
            customer.setName(name);
            customer = customerRepository.save(customer);
        }
        return customer;
    }
    /**
     * Search customer by mobile number
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerByMobile(String mobile) {
        Customer customer = customerRepository.findByMobile(mobile)
                .orElse(null);
        return customer != null ? CustomerDTO.fromEntity(customer) : null;
    }
    /**
     * Search customers by mobile or name
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> searchCustomers(String search) {
        return customerRepository.searchByMobileOrName(search)
                .stream()
                .map(CustomerDTO::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * Get all customers
     */
    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAllOrderByPointsDesc()
                .stream()
                .map(CustomerDTO::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * Credit points to customer after order (10% of order total)
     */
    @Transactional
    public CustomerDTO creditPointsForOrder(Long customerId, Long orderId, BigDecimal orderTotal) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        // Calculate points: 10% of order total (1 rupee = 1 point before 10x multiplier for display)
        // Actually: 10% of order total becomes points directly
        int pointsToCredit = orderTotal.multiply(POINTS_EARN_PERCENTAGE)
                .setScale(0, RoundingMode.FLOOR)
                .intValue();
        if (pointsToCredit <= 0) {
            return CustomerDTO.fromEntity(customer);
        }
        // Update customer total points
        int newBalance = customer.getTotalPoints() + pointsToCredit;
        customer.setTotalPoints(newBalance);
        customer = customerRepository.save(customer);
        // Create ledger entry
        CustomerPointsLedger ledger = new CustomerPointsLedger();
        ledger.setCustomer(customer);
        ledger.setOrderId(orderId);
        ledger.setPoints(pointsToCredit);
        ledger.setTransactionType(CustomerPointsLedger.TransactionType.CREDIT);
        ledger.setDescription("Points earned from Order #" + orderId + " (10% of ₹" + orderTotal + ")");
        ledger.setBalanceAfter(newBalance);
        pointsLedgerRepository.save(ledger);
        log.info("Credited {} points to customer {} for order {}. New balance: {}",
                pointsToCredit, customerId, orderId, newBalance);
        return CustomerDTO.fromEntity(customer);
    }
    /**
     * Redeem points for order discount (10 points = 1 rupee)
     */
    @Transactional
    public RedeemPointsResponse redeemPoints(Long customerId, Integer pointsToRedeem, Long orderId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        if (pointsToRedeem <= 0) {
            throw new RuntimeException("Points to redeem must be positive");
        }
        if (customer.getTotalPoints() < pointsToRedeem) {
            throw new RuntimeException("Insufficient points. Available: " + customer.getTotalPoints());
        }
        // Calculate discount: 10 points = 1 rupee
        BigDecimal discountAmount = new BigDecimal(pointsToRedeem)
                .divide(BigDecimal.valueOf(POINTS_PER_RUPEE), 2, RoundingMode.FLOOR);
        // Update customer total points
        int newBalance = customer.getTotalPoints() - pointsToRedeem;
        customer.setTotalPoints(newBalance);
        customer = customerRepository.save(customer);
        // Create ledger entry
        CustomerPointsLedger ledger = new CustomerPointsLedger();
        ledger.setCustomer(customer);
        ledger.setOrderId(orderId);
        ledger.setPoints(pointsToRedeem);
        ledger.setTransactionType(CustomerPointsLedger.TransactionType.DEBIT);
        ledger.setDescription("Points redeemed for Order #" + orderId + " (₹" + discountAmount + " discount)");
        ledger.setBalanceAfter(newBalance);
        pointsLedgerRepository.save(ledger);
        log.info("Redeemed {} points from customer {} for order {}. Discount: ₹{}. New balance: {}",
                pointsToRedeem, customerId, orderId, discountAmount, newBalance);
        RedeemPointsResponse response = new RedeemPointsResponse();
        response.setPointsRedeemed(pointsToRedeem);
        response.setDiscountAmount(discountAmount);
        response.setRemainingPoints(newBalance);
        response.setCustomer(CustomerDTO.fromEntity(customer));
        return response;
    }
    /**
     * Get customer points ledger/history
     */
    @Transactional(readOnly = true)
    public List<CustomerPointsLedgerDTO> getCustomerPointsHistory(Long customerId) {
        return pointsLedgerRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream()
                .map(CustomerPointsLedgerDTO::fromEntity)
                .collect(Collectors.toList());
    }
    /**
     * Calculate maximum redeemable amount for a customer
     */
    @Transactional(readOnly = true)
    public BigDecimal getMaxRedeemableAmount(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return new BigDecimal(customer.getTotalPoints())
                .divide(BigDecimal.valueOf(POINTS_PER_RUPEE), 2, RoundingMode.FLOOR);
    }
    /**
     * Get customer by ID
     */
    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return CustomerDTO.fromEntity(customer);
    }

    /**
     * Credit points directly to customer
     */
    @Transactional
    public void creditPoints(Long customerId, int points, String description, Long orderId) {
        if (points <= 0) return;

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        int newBalance = customer.getTotalPoints() + points;
        customer.setTotalPoints(newBalance);
        customerRepository.save(customer);
        CustomerPointsLedger ledger = new CustomerPointsLedger();
        ledger.setCustomer(customer);
        ledger.setOrderId(orderId);
        ledger.setPoints(points);
        ledger.setTransactionType(CustomerPointsLedger.TransactionType.CREDIT);
        ledger.setDescription(description);
        ledger.setBalanceAfter(newBalance);
        pointsLedgerRepository.save(ledger);
        log.info("Credited {} points to customer {}. New balance: {}", points, customerId, newBalance);
    }
    /**
     * Debit points directly from customer
     */
    @Transactional
    public void debitPoints(Long customerId, int points, String description, Long orderId) {
        if (points <= 0) return;

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        if (customer.getTotalPoints() < points) {
            throw new RuntimeException("Insufficient points. Available: " + customer.getTotalPoints());
        }
        int newBalance = customer.getTotalPoints() - points;
        customer.setTotalPoints(newBalance);
        customerRepository.save(customer);
        CustomerPointsLedger ledger = new CustomerPointsLedger();
        ledger.setCustomer(customer);
        ledger.setOrderId(orderId);
        ledger.setPoints(points);
        ledger.setTransactionType(CustomerPointsLedger.TransactionType.DEBIT);
        ledger.setDescription(description);
        ledger.setBalanceAfter(newBalance);
        pointsLedgerRepository.save(ledger);
        log.info("Debited {} points from customer {}. New balance: {}", points, customerId, newBalance);
    }


    /**
     * Get complete customer details with all orders by mobile number
     */
    @Transactional(readOnly = true)
    public CustomerOrdersResponse getCustomerOrdersByMobile(String mobile) {
        Customer customer = customerRepository.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("Customer not found with mobile: " + mobile));

        CustomerOrdersResponse response = new CustomerOrdersResponse();

        // Set customer details
        response.setCustomerId(customer.getId());
        response.setCustomerName(customer.getName());
        response.setCustomerMobile(customer.getMobile());
        response.setTotalPoints(customer.getTotalPoints() != null ? customer.getTotalPoints() : 0);
        response.setRedeemableAmount(new BigDecimal(response.getTotalPoints())
                .divide(BigDecimal.TEN, 2, RoundingMode.FLOOR));
        response.setCustomerCreatedAt(customer.getCreatedAt());

        // Get all orders for this customer
        List<Order> orders = orderRepository.findByCustomerMobileOrderByCreatedAtDesc(mobile);

        // Calculate totals
        int totalPointsEarned = 0;
        int totalPointsRedeemed = 0;
        BigDecimal totalOrderAmount = BigDecimal.ZERO;

        List<CustomerOrdersResponse.CustomerOrderDTO> orderDTOs = new ArrayList<>();

        for (Order order : orders) {
            CustomerOrdersResponse.CustomerOrderDTO orderDTO = new CustomerOrdersResponse.CustomerOrderDTO();
            orderDTO.setOrderId(order.getId());
            orderDTO.setTotalAmount(order.getTotalAmount());
            orderDTO.setSubtotal(order.getSubTotal());
            orderDTO.setTotalGst(order.getTotalGst());
            orderDTO.setTotalDiscount(order.getTotalDiscount());
            orderDTO.setPointsDiscount(order.getPointsDiscount());
            orderDTO.setPointsUsed(order.getPointsUsed());
            orderDTO.setPointsEarned(order.getPointsEarned());
            orderDTO.setStatus(order.getStatus() != null ? order.getStatus().name() : null);
            orderDTO.setPaymentMethod(order.getPaymentMethod());
            orderDTO.setPaymentStatus(order.getPaymentStatus() != null ? order.getPaymentStatus().name() : null);
            orderDTO.setCreatedAt(order.getCreatedAt());

            // Map order items
            List<OrderItemDTO> itemDTOs = new ArrayList<>();
            if (order.getOrderItems() != null) {
                for (OrderItem item : order.getOrderItems()) {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProductId());
                    itemDTO.setProductName(item.getProductName());
                    itemDTO.setProductImage(item.getProductImage());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());
                    itemDTO.setSize(item.getSize());
                    itemDTO.setGstPercentage(item.getGstPercentage());
                    itemDTO.setGstAmount(item.getGstAmount());
                    itemDTO.setSubTotal(item.getSubTotal());
                    itemDTO.setTotalAmount(item.getTotalAmount());
                    itemDTO.setDiscountAmount(item.getDiscountAmount());
                    itemDTO.setAppliedOfferName(item.getAppliedOfferName());
                    itemDTO.setBillableQty(item.getBillableQty());
                    itemDTO.setDeliveredQty(item.getDeliveredQty());
                    itemDTOs.add(itemDTO);
                }
            }
            orderDTO.setItems(itemDTOs);
            orderDTOs.add(orderDTO);

            // Accumulate totals
            totalPointsEarned += (order.getPointsEarned() != null ? order.getPointsEarned() : 0);
            totalPointsRedeemed += (order.getPointsUsed() != null ? order.getPointsUsed() : 0);
            totalOrderAmount = totalOrderAmount.add(order.getTotalAmount() != null ? order.getTotalAmount() : BigDecimal.ZERO);
        }

        response.setOrders(orderDTOs);
        response.setTotalOrders(orders.size());
        response.setTotalPointsEarned(totalPointsEarned);
        response.setTotalPointsRedeemed(totalPointsRedeemed);
        response.setTotalOrderAmount(totalOrderAmount);

        // Get points history
        List<CustomerPointsLedgerDTO> pointsHistory = pointsLedgerRepository
                .findByCustomerIdOrderByCreatedAtDesc(customer.getId())
                .stream()
                .map(CustomerPointsLedgerDTO::fromEntity)
                .collect(Collectors.toList());
        response.setPointsHistory(pointsHistory);

        return response;
    }

    /**
     * Get complete customer details with all orders by customer ID
     */
    @Transactional(readOnly = true)
    public CustomerOrdersResponse getCustomerOrdersById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found: " + customerId));
        return getCustomerOrdersByMobile(customer.getMobile());
    }
}