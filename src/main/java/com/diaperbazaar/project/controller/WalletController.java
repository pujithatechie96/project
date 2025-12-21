package com.diaperbazaar.project.controller;

import com.diaperbazaar.project.entity.Wallet;
import com.diaperbazaar.project.entity.WalletTransaction;
import com.diaperbazaar.project.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<Wallet> getWallet(@RequestParam Long userId) {
        Wallet wallet = walletService.getOrCreateWallet(userId);
        return ResponseEntity.ok(wallet);
    }

    @GetMapping("/transactions")
    public ResponseEntity<List<WalletTransaction>> getTransactions(@RequestParam Long userId) {
        List<WalletTransaction> transactions = walletService.getTransactions(userId);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/redeem")
    public ResponseEntity<?> redeemPoints(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.parseLong(request.get("userId").toString());
            int points = Integer.parseInt(request.get("points").toString());
            Long orderId = request.get("orderId") != null
                    ? Long.parseLong(request.get("orderId").toString())
                    : null;

            Wallet wallet = walletService.debitPoints(userId, points,
                    "Points redeemed at checkout", orderId);
            return ResponseEntity.ok(wallet);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
