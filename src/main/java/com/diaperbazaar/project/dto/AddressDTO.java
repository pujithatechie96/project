package com.diaperbazaar.project.dto;


import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class AddressDTO {
    private Long id;
    private Long userId;

    @NotBlank(message = "Label is required")
    @Size(max = 50)
    private String label;

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "Pincode is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "Pincode must be 6 digits")
    private String pincode;

    private Boolean isDefault = false;
    private String createdAt;
    private String updatedAt;
}