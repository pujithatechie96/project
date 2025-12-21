package com.diaperbazaar.project.repository;


import com.diaperbazaar.project.entity.PasswordResetOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PasswordResetOtpRepository extends JpaRepository<PasswordResetOtp, Long> {

    Optional<PasswordResetOtp> findByUserIdAndOtpCodeAndIsUsedFalse(Long userId, String otpCode);

    @Modifying
    @Transactional
    @Query("UPDATE PasswordResetOtp o SET o.isUsed = true WHERE o.userId = :userId AND o.isUsed = false")
    void invalidateAllOtpsForUser(Long userId);

    @Modifying
    @Transactional
    @Query("DELETE FROM PasswordResetOtp o WHERE o.expiresAt < :dateTime")
    void deleteExpiredOtps(LocalDateTime dateTime);
}