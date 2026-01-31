package com.diaperbazaar.project.dto;

import com.diaperbazaar.project.entity.Role;
import com.diaperbazaar.project.entity.User;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponseDTO {

    private Long id;
    private String name;
    private String email;
    private User.UserRole role;
    private Boolean emailVerified;
}
