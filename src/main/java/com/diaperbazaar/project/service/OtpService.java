package com.diaperbazaar.project.service;


import com.diaperbazaar.project.entity.PasswordResetOtp;
import com.diaperbazaar.project.repository.PasswordResetOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final PasswordResetOtpRepository otpRepository;

    @Value("${otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Value("${otp.length:6}")
    private int otpLength;

    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public String generateOtp(Long userId) {
        // Invalidate any existing OTPs for this user
        otpRepository.invalidateAllOtpsForUser(userId);

        // Generate new 6-digit OTP
        String otpCode = generateSecureOtp();

        // Save OTP
        PasswordResetOtp otp = new PasswordResetOtp();
        otp.setUserId(userId);
        otp.setOtpCode(otpCode);
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(otpExpirationMinutes));
        otp.setIsUsed(false);

        otpRepository.save(otp);

        return otpCode;
    }

    @Transactional(readOnly = true)
    public boolean verifyOtp(Long userId, String otpCode) {
        Optional<PasswordResetOtp> otpOptional = otpRepository
                .findByUserIdAndOtpCodeAndIsUsedFalse(userId, otpCode);

        if (otpOptional.isEmpty()) {
            return false;
        }

        PasswordResetOtp otp = otpOptional.get();
        return otp.isValid();
    }

    @Transactional
    public void markOtpAsUsed(Long userId, String otpCode) {
        Optional<PasswordResetOtp> otpOptional = otpRepository
                .findByUserIdAndOtpCodeAndIsUsedFalse(userId, otpCode);

        otpOptional.ifPresent(otp -> {
            otp.setIsUsed(true);
            otpRepository.save(otp);
        });
    }

    private String generateSecureOtp() {
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(secureRandom.nextInt(10));
        }
        return otp.toString();
    }

    // Cleanup expired OTPs every hour
    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteExpiredOtps(LocalDateTime.now());
    }
}