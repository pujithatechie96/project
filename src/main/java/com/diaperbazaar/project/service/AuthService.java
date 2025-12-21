package com.diaperbazaar.project.service;

import com.diaperbazaar.project.dto.*;
import com.diaperbazaar.project.entity.Role;
import com.diaperbazaar.project.entity.User;
import com.diaperbazaar.project.exception.BadRequestException;
import com.diaperbazaar.project.exception.UnauthorizedException;
import com.diaperbazaar.project.repository.RoleRepository;
import com.diaperbazaar.project.repository.UserRepository;
import com.diaperbazaar.project.util.JwtUtil;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@Slf4j
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;  // Your existing JwtUtil

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private OtpService otpService;  // NEW

    @Autowired
    private EmailService emailService;  // NEW

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BadRequestException("Email already registered. Please login or use a different email.");
        }

        // Create new user
        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.USER);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);

        // Send welcome email asynchronously (NEW)
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());

        log.info("New user registered: {}", savedUser.getEmail());

        // Generate token using your existing JwtUtil
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

        AuthResponse response = new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
        return response;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Generate token using your existing JwtUtil
        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        log.info("User logged in: {}", user.getEmail());

        return new AuthResponse(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ==================== NEW METHODS ====================

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElse(null);

        // Always return success message to prevent email enumeration
        if (user == null) {
            log.warn("Password reset requested for non-existent email: {}", request.getEmail());
            return new MessageResponse("If an account exists with this email, you will receive a password reset code.", true);
        }

        // Generate OTP
        String otpCode = otpService.generateOtp(user.getId());

        // Send OTP email
        emailService.sendPasswordResetOtp(user.getEmail(), user.getName(), otpCode);

        log.info("Password reset OTP sent to: {}", user.getEmail());

        return new MessageResponse("If an account exists with this email, you will receive a password reset code.", true);
    }

    @Transactional(readOnly = true)
    public MessageResponse verifyOtp(VerifyOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid request"));

        boolean isValid = otpService.verifyOtp(user.getId(), request.getOtp());

        if (!isValid) {
            throw new BadRequestException("Invalid or expired verification code. Please request a new one.");
        }

        return new MessageResponse("Verification code is valid. You can now reset your password.", true);
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BadRequestException("Invalid request"));

        // Verify OTP
        boolean isValid = otpService.verifyOtp(user.getId(), request.getOtp());

        if (!isValid) {
            throw new BadRequestException("Invalid or expired verification code. Please request a new one.");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark OTP as used
        otpService.markOtpAsUsed(user.getId(), request.getOtp());

        log.info("Password reset successful for: {}", user.getEmail());

        return new MessageResponse("Password reset successful! You can now login with your new password.", true);
    }
}