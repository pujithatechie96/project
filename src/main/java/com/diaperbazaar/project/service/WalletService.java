package com.diaperbazaar.project.service;

import com.diaperbazaar.project.entity.Wallet;
import com.diaperbazaar.project.entity.WalletTransaction;
import com.diaperbazaar.project.repository.WalletRepository;
import com.diaperbazaar.project.repository.WalletTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletTransactionRepository transactionRepository;

    // Points conversion: 1 point = ₹1
    public static final int POINTS_TO_RUPEES_RATIO = 1;
    // Points earned: 10% of order value
    public static final double POINTS_EARN_PERCENTAGE = 0.10;

    public Wallet getOrCreateWallet(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUserId(userId);
                    wallet.setBalance(0);
                    return walletRepository.save(wallet);
                });
    }

    public Wallet getWallet(Long userId) {
        return walletRepository.findByUserId(userId).orElse(null);
    }

    public List<WalletTransaction> getTransactions(Long userId) {
        Wallet wallet = getOrCreateWallet(userId);
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(wallet.getId());
    }

    /**
     * Credit points to wallet (e.g., after order placement)
     * Points earned = 10% of order subtotal
     */
    @Transactional
    public Wallet creditPoints(Long userId, int points, String description, Long orderId) {
        Wallet wallet = getOrCreateWallet(userId);
        wallet.setBalance(wallet.getBalance() + points);
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType(WalletTransaction.TransactionType.CREDIT);
        transaction.setPoints(points);
        transaction.setDescription(description);
        transaction.setOrderId(orderId);
        transactionRepository.save(transaction);

        return wallet;
    }

    /**
     * Debit points from wallet (e.g., redeem at checkout)
     * 1 point = ₹1 discount
     */
    @Transactional
    public Wallet debitPoints(Long userId, int points, String description, Long orderId) {
        Wallet wallet = getOrCreateWallet(userId);

        if (wallet.getBalance() < points) {
            throw new RuntimeException("Insufficient points balance");
        }

        wallet.setBalance(wallet.getBalance() - points);
        walletRepository.save(wallet);

        // Create transaction record
        WalletTransaction transaction = new WalletTransaction();
        transaction.setWalletId(wallet.getId());
        transaction.setType(WalletTransaction.TransactionType.DEBIT);
        transaction.setPoints(points);
        transaction.setDescription(description);
        transaction.setOrderId(orderId);
        transactionRepository.save(transaction);

        return wallet;
    }

    /**
     * Calculate points to earn from order
     * Returns 10% of order subtotal as points
     */
    public int calculatePointsToEarn(BigDecimal orderSubtotal) {
        return orderSubtotal.multiply(BigDecimal.valueOf(POINTS_EARN_PERCENTAGE)).intValue();
    }

    /**
     * Calculate rupee value of points
     * 1 point = ₹1
     */
    public int getPointsValue(int points) {
        return points * POINTS_TO_RUPEES_RATIO;
    }
}