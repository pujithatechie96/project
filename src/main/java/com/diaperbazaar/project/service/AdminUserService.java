package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.AdminUserRequestDTO;
import com.diaperbazaar.project.dto.UserResponseDTO;
import com.diaperbazaar.project.entity.User;
import com.diaperbazaar.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponseDTO createUser(AdminUserRequestDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(dto.getRole())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .build();

        userRepository.save(user);

        return mapToResponse(user);
    }

    public UserResponseDTO updateUser(Long userId, AdminUserRequestDTO dto) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setName(dto.getName());
        user.setRole(dto.getRole());
        user.setPhone(dto.getPhone());
        user.setAddress(dto.getAddress());
        user.setCity(dto.getCity());
        user.setState(dto.getState());
        user.setZipCode(dto.getZipCode());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        userRepository.save(user);
        return mapToResponse(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .emailVerified(user.getEmailVerified())
                .build();
    }


    public List<UserResponseDTO> searchUsers(String email, String phone) {

        List<User> users;

        if (email != null && phone != null) {
            users = userRepository
                    .findByEmailContainingIgnoreCaseOrPhoneContaining(email, phone);
        } else if (email != null) {
            users = userRepository
                    .findByEmailContainingIgnoreCase(email);
        } else if (phone != null) {
            users = userRepository
                    .findByPhoneContaining(phone);
        } else {
            users = userRepository.findAll();
        }

        return users.stream()
                .map(this::mapToResponse)
                .toList();
    }

}
