package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.Role;
import com.diaperbazaar.project.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminUserRequestDTO {

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    private String password; // optional for update

    private User.UserRole role; // ADMIN or USER (editable)

    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
}
