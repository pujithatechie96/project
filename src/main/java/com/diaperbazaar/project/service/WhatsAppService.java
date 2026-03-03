package com.diaperbazaar.project.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class WhatsAppService {

    @Value("${whatsapp.token}")
    private String token;

    @Value("${whatsapp.phoneId}")
    private String phoneId;

    @Value("${whatsapp.url}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Send invoice with item summary via WhatsApp Utility template
     */
    public void sendInvoiceWithItems(String mobile,
                                     String customerName,
                                     String orderId,
                                     String itemsSummary,
                                     String amount,
                                     String trackingLink) {

        try {
            String url = baseUrl + phoneId + "/messages";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);

            Map<String, Object> bodyParams = Map.of(
                    "type", "body",
                    "parameters", List.of(
                            text(customerName),
                            text(orderId),
                            text(itemsSummary),
                            text(amount),
                            text(trackingLink)
                    )
            );

            Map<String, Object> template = Map.of(
                    "name", "db_invoice_items",
                    "language", Map.of("code", "en"),
                    "components", List.of(bodyParams)
            );

            Map<String, Object> payload = Map.of(
                    "messaging_product", "whatsapp",
                    "to", "91" + mobile,
                    "type", "template",
                    "template", template
            );

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(url, request, String.class);

        } catch (Exception e) {
            // Never break order flow if WhatsApp fails
            System.err.println("WhatsApp send failed: " + e.getMessage());
        }
    }

    // Helper to create WhatsApp text parameter
    private Map<String, Object> text(String value) {
        return Map.of("type", "text", "text", value);
    }

    /**
     * Compact item summary for WhatsApp
     */
    public String buildItemSummary(List<?> orderItems) {
        StringBuilder sb = new StringBuilder();

        for (Object obj : orderItems) {
            if (obj instanceof com.diaperbazaar.project.entity.OrderItem item) {
                sb.append(item.getProductName())
                        .append(" (")
                        .append(item.getDeliveredQty())
                        .append(" x ")
                        .append(item.getSize())
                        .append("), ");
            }
        }

        if (sb.length() > 2) sb.setLength(sb.length() - 2);

        // WhatsApp body safety
        if (sb.length() > 400) {
            return sb.substring(0, 400) + "...";
        }

        return sb.toString();
    }
}
