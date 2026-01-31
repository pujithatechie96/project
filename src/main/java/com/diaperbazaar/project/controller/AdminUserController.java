package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.dto.AdminUserRequestDTO;
import com.diaperbazaar.project.dto.UserResponseDTO;
import com.diaperbazaar.project.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    // CREATE USER
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
            @Valid @RequestBody AdminUserRequestDTO dto) {
        return ResponseEntity.ok(adminUserService.createUser(dto));
    }

    // UPDATE USER (ROLE EDITABLE)
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserRequestDTO dto) {
        return ResponseEntity.ok(adminUserService.updateUser(userId, dto));
    }

    // DELETE USER
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponseDTO>> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String phone) {

        return ResponseEntity.ok(
                adminUserService.searchUsers(email, phone)
        );
    }

}
