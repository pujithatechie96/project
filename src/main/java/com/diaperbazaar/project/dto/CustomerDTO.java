package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.Customer;
import lombok.Data;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String mobile;
    private Integer totalPoints;
    private BigDecimal redeemableAmount; // Points / 10 = Rupees
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    public static CustomerDTO fromEntity(Customer customer) {
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setMobile(customer.getMobile());
        dto.setTotalPoints(customer.getTotalPoints() != null ? customer.getTotalPoints() : 0);
        // 10 points = 1 rupee
        dto.setRedeemableAmount(new BigDecimal(dto.getTotalPoints())
                .divide(BigDecimal.TEN, 2, RoundingMode.FLOOR));
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }
}