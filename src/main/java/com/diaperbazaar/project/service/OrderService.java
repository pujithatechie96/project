package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.*;
import com.diaperbazaar.project.entity.*;
import com.diaperbazaar.project.repository.*;
import com.diaperbazaar.project.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductVariantRepository productVariantRepository;
    private final WalletService walletService;
    private final OfferService offerService;

    private final StockTransactionRepository stockTransactionRepository;
    private final StockService stockService;
    private final UserRepository userRepository;
    private final CustomerService customerService;


    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {
        BigDecimal orderSubtotal = BigDecimal.ZERO;
        BigDecimal orderTotalGst = BigDecimal.ZERO;
        BigDecimal orderTotalDiscount = BigDecimal.ZERO;

        // Check if user is admin for customer loyalty handling
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        boolean isAdmin = "ADMIN".equalsIgnoreCase(user.getRole().name());

        // Fetch address and convert to ShippingAddress JSON
        Address address = addressRepository.findById(request.getAddressId())
                .orElseThrow(() -> new RuntimeException("Address not found"));

        // Verify address belongs to user
        if (!address.getUserId().equals(userId)) {
            throw new RuntimeException("Address does not belong to user");
        }

        // Create ShippingAddress from Address for backward compatibility
        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFullName(address.getFullName());
        shippingAddress.setPhone(address.getPhone());
        shippingAddress.setAddressLine1(address.getAddressLine1());
        shippingAddress.setAddressLine2(address.getAddressLine2());
        shippingAddress.setCity(address.getCity());
        shippingAddress.setState(address.getState());
        shippingAddress.setZipCode(address.getPincode());

        // Create order
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(Order.OrderStatus.PENDING);
        order.setPaymentMethod(request.getPaymentMethod());
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setAddressId(request.getAddressId());
        order.setShippingAddress(shippingAddress);
        order.setPointsUsed(request.getPointsUsed() != null ? request.getPointsUsed() : 0);

        // 🔥 For admin orders: Set customer details if provided
        if (isAdmin && request.getCustomerMobile() != null) {
            order.setCustomerName(request.getCustomerName());
            order.setCustomerMobile(request.getCustomerMobile());
            order.setCustomerPointsRedeemed(request.getPointsUsed() != null ? request.getPointsUsed() : 0);
        }

        // Process each order item with offer engine
        for (var itemRequest : request.getItems()) {
            ProductVariant variant = productVariantRepository.findById(itemRequest.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + itemRequest.getVariantId()));

            // 🔥 OFFER ENGINE INTEGRATION
            // Apply best offer for this variant
            BigDecimal unitPrice = itemRequest.getPrice();
            int orderedQty = itemRequest.getQuantity();

            OfferResult offerResult = offerService.applyBestOffer(
                    itemRequest.getVariantId(),
                    orderedQty,
                    unitPrice
            );

            log.info("Offer applied for variant {}: billableQty={}, deliveredQty={}, subtotal={}, discount={}, offer={}",
                    itemRequest.getVariantId(),
                    offerResult.getBillableQty(),
                    offerResult.getDeliveredQty(),
                    offerResult.getSubtotal(),
                    offerResult.getDiscountAmount(),
                    offerResult.getAppliedOfferName());

            // 🔥 GST CALCULATION - Applied AFTER discount
            // GST is calculated on the discounted subtotal, not original price
            BigDecimal gstPercentage = variant.getGstPercentage() != null
                    ? variant.getGstPercentage()
                    : BigDecimal.ZERO;

            BigDecimal gstAmount = offerResult.getSubtotal()
                    .multiply(gstPercentage)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal itemTotal = offerResult.getSubtotal().add(gstAmount);

            // Create order item
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setProductImage(itemRequest.getProductImage());
            item.setQuantity(offerResult.getDeliveredQty()); // Delivered qty includes free items
            item.setPrice(unitPrice);
            item.setSize(itemRequest.getSize());
            item.setVariant(variant);

            // 🔥 Store offer and GST details in order item
            item.setGstPercentage(gstPercentage);
            item.setGstAmount(gstAmount);
            item.setSubTotal(offerResult.getSubtotal());
            item.setTotalAmount(itemTotal);
            item.setDiscountAmount(offerResult.getDiscountAmount());
            item.setAppliedOfferName(offerResult.getAppliedOfferName());
            item.setAppliedOfferId(offerResult.getAppliedOfferId());
            item.setBillableQty(offerResult.getBillableQty());
            item.setDeliveredQty(offerResult.getDeliveredQty());

            order.addOrderItem(item);

            // Accumulate order totals
            orderSubtotal = orderSubtotal.add(offerResult.getSubtotal());
            orderTotalGst = orderTotalGst.add(gstAmount);
            orderTotalDiscount = orderTotalDiscount.add(offerResult.getDiscountAmount());

            // Get partyId from last purchase transaction for this product/variant
            List<Long> partyIds = stockTransactionRepository.findLastPurchasePartyIds(
                    itemRequest.getProductId(), itemRequest.getVariantId());
            Long partyId = partyIds.isEmpty() ? null : partyIds.get(0);

            // 🔥 CRITICAL: Reduce inventory by DELIVERED quantity (includes free items)
            stockService.addSale(
                    itemRequest.getProductId(),
                    itemRequest.getVariantId(),
                    partyId,
                    offerResult.getDeliveredQty(), // Use delivered qty for stock reduction
                    unitPrice,
                    "Online Order - " + itemRequest.getProductName() +
                            (offerResult.isOfferApplied() ? " [" + offerResult.getAppliedOfferName() + "]" : "")
            );

            // 🔥 Handle FREE_ITEM offer type - add free items to inventory reduction
            if (offerResult.getFreeItems() != null && !offerResult.getFreeItems().isEmpty()) {
                for (OfferResult.FreeItem freeItem : offerResult.getFreeItems()) {
                    List<Long> freeItemPartyIds = stockTransactionRepository.findLastPurchasePartyIds(
                            itemRequest.getProductId(), freeItem.getProductVariantId());
                    Long freeItemPartyId = freeItemPartyIds.isEmpty() ? null : freeItemPartyIds.get(0);

                    stockService.addSale(
                            itemRequest.getProductId(),
                            freeItem.getProductVariantId(),
                            freeItemPartyId,
                            freeItem.getQuantity(),
                            BigDecimal.ZERO, // Free items have zero price
                            "Free Item - " + freeItem.getVariantTitle() + " [" + offerResult.getAppliedOfferName() + "]"
                    );
                }
            }
        }

        // Apply points discount if provided (1 point = ₹1)
        BigDecimal pointsDiscount = request.getPointsDiscount() != null
                ? request.getPointsDiscount()
                : BigDecimal.ZERO;

        // Calculate final order total
        BigDecimal orderTotalBeforePoints = orderSubtotal.add(orderTotalGst);
        BigDecimal finalAmount = orderTotalBeforePoints.subtract(pointsDiscount);

        // Ensure final amount is not negative
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
            finalAmount = BigDecimal.ZERO;
        }

        // Calculate points earned: 10% of final amount
        int pointsEarned = finalAmount.multiply(new BigDecimal("0.10")).intValue();

        // Set order totals
        order.setTotalAmount(finalAmount);
        order.setSubTotal(orderSubtotal);
        order.setTotalGst(orderTotalGst);
        order.setTotalDiscount(orderTotalDiscount);
        order.setPointsDiscount(pointsDiscount);
        order.setPointsEarned(pointsEarned);

        // 🔥 For admin orders: Set customer points earned
        if (isAdmin && request.getCustomerMobile() != null) {
            order.setCustomerPointsEarned(pointsEarned);
        }

        // Save order
        Order savedOrder = orderRepository.save(order);

        // 🔥 Handle points based on user type
        if (isAdmin && request.getCustomerMobile() != null) {
            // ADMIN/OFFLINE ORDER: Use CustomerService for customer loyalty
            Customer customer = customerService.getOrCreateCustomers(
                    request.getCustomerName(),
                    request.getCustomerMobile()
            );
            order.setCustomerId(customer.getId());

            // Credit points to customer
            customerService.creditPoints(
                    customer.getId(),
                    pointsEarned,
                    "Points earned from Offline Order #" + savedOrder.getId(),
                    savedOrder.getId()
            );

            // Debit redeemed points from customer
            if (request.getPointsUsed() != null && request.getPointsUsed() > 0) {
                customerService.debitPoints(
                        customer.getId(),
                        request.getPointsUsed(),
                        "Points redeemed for Offline Order #" + savedOrder.getId(),
                        savedOrder.getId()
                );
            }

            log.info("Offline Order #{} - Customer: {} ({}), Points Earned: {}, Points Redeemed: {}",
                    savedOrder.getId(), customer.getName(), customer.getMobile(),
                    pointsEarned, request.getPointsUsed());
        } else {
            // ONLINE ORDER: Use WalletService for user wallet
            walletService.creditPoints(
                    userId,
                    pointsEarned,
                    "Points earned from Order #" + savedOrder.getId(),
                    savedOrder.getId()
            );

            if (request.getPointsUsed() != null && request.getPointsUsed() > 0) {
                walletService.debitPoints(
                        userId,
                        request.getPointsUsed(),
                        "Points redeemed for Order #" + savedOrder.getId(),
                        savedOrder.getId()
                );
            }
        }

        log.info("Order #{} created successfully. Subtotal: {}, GST: {}, Discount: {}, Final: {}",
                savedOrder.getId(), orderSubtotal, orderTotalGst, orderTotalDiscount, finalAmount);

        return convertToDTO(savedOrder);
    }


    @Transactional(readOnly = true)
    public List<OrderDTO> getUserOrders(Long userId) {
        List<Order> orders = orderRepository.findByUserIdWithItems(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findByIdWithItems(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("Order not found");
        }
        return convertToDTO(order);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order updatedOrder = orderRepository.save(order);
        return convertToDTO(updatedOrder);
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setUserId(order.getUserId());
        dto.setTotalAmount(order.getTotalAmount());
        dto.setStatus(order.getStatus());
        dto.setPaymentMethod(order.getPaymentMethod());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setAddressId(order.getAddressId());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setPointsUsed(order.getPointsUsed());
        dto.setPointsDiscount(order.getPointsDiscount());
        dto.setPointsEarned(order.getPointsEarned());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUpdatedAt(order.getUpdatedAt());

        List<OrderItemDTO> items = order.getOrderItems().stream()
                .map(item -> {
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setId(item.getId());
                    itemDTO.setProductId(item.getProductId());
                    itemDTO.setVariant(item.getVariant());
                    itemDTO.setProductName(item.getProductName());
                    itemDTO.setProductImage(item.getProductImage());
                    itemDTO.setQuantity(item.getQuantity());
                    itemDTO.setPrice(item.getPrice());
                    itemDTO.setSize(item.getSize());
                    itemDTO.setGstPercentage(item.getGstPercentage());
                    itemDTO.setGstAmount(item.getGstAmount());
                    itemDTO.setTotalAmount(item.getTotalAmount());
                    itemDTO.setSubTotal(item.getSubTotal());
                    itemDTO.setDiscountAmount(item.getDiscountAmount());
                    itemDTO.setAppliedOfferId(item.getAppliedOfferId());
                    itemDTO.setAppliedOfferName(item.getAppliedOfferName());
                    itemDTO.setBillableQty(item.getBillableQty());
                    itemDTO.setDeliveredQty(item.getDeliveredQty());
                    return itemDTO;
                })
                .collect(Collectors.toList());

        dto.setOrderItems(items);
        return dto;
    }


    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(OrderDTO::fromEntity)
                .toList();
    }


    @Transactional(readOnly = true)
    public OrderDTO getOrderByIdAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public OrderDTO updateOrderAdmin(Long orderId, UpdateOrderRequest request) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());
        }

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId()).get();
            order.setAddressId(request.getAddressId());
            ShippingAddress shippingAddress = new ShippingAddress();
            shippingAddress.setFullName(address.getFullName());
            shippingAddress.setPhone(address.getPhone());
            shippingAddress.setAddressLine1(address.getAddressLine1());
            shippingAddress.setAddressLine2(address.getAddressLine2());
            shippingAddress.setCity(address.getCity());
            shippingAddress.setState(address.getState());
            shippingAddress.setZipCode(address.getPincode());
            order.setShippingAddress(shippingAddress);
        }

        if (request.getPaymentMethod() != null) {
            order.setPaymentMethod(request.getPaymentMethod());
        }

        return OrderDTO.fromEntity(orderRepository.save(order));
    }
}
