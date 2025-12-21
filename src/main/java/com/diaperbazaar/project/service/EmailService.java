package com.diaperbazaar.project.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // ===================== PUBLIC METHODS =====================

    @Async
    public void sendWelcomeEmail(String toEmail, String userName) {
        String subject = "Welcome to DiaperBazaar! 🎉";
        String body = buildWelcomeEmailTemplate(userName);
        sendHtmlEmail(toEmail, subject, body);
    }

    @Async
    public void sendPasswordResetOtp(String toEmail, String userName, String otpCode) {
        String subject = "Your Password Reset Code - DiaperBazaar";
        String body = buildPasswordResetEmailTemplate(userName, otpCode);
        sendHtmlEmail(toEmail, subject, body);
    }

    // ===================== CORE MAIL METHOD =====================

    private void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Email sent successfully to {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}", toEmail, e);
        }
    }

    // ===================== WELCOME EMAIL TEMPLATE =====================

    private String buildWelcomeEmailTemplate(String userName) {
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Welcome</title>
</head>
<body style="font-family: Arial, sans-serif; background:#f4f6f8; padding:20px;">
  <table width="100%%" style="max-width:600px;margin:auto;background:#ffffff;border-radius:8px;">
    <tr>
      <td style="background:#6366f1;color:white;padding:30px;text-align:center;">
        <h1>🍼 DiaperBazaar</h1>
        <p>Your Trusted Baby Care Partner</p>
      </td>
    </tr>
    <tr>
      <td style="padding:30px;">
        <h2>Welcome, %s 🎉</h2>
        <p>
          We’re excited to have you at <b>DiaperBazaar</b>.
          Your account is ready and you can start shopping now!
        </p>

        <ul>
          <li>Premium baby & adult care products</li>
          <li>Exclusive discounts</li>
          <li>Fast & secure delivery</li>
        </ul>

        <div style="text-align:center;margin:30px 0;">
          <a href="https://diaperbazaar.com"
             style="background:#6366f1;color:white;
             padding:12px 25px;border-radius:6px;
             text-decoration:none;font-weight:bold;">
            Start Shopping
          </a>
        </div>

        <p>Need help? We’re always here for you.</p>
      </td>
    </tr>
    <tr>
      <td style="background:#1f2937;color:#9ca3af;padding:20px;text-align:center;">
        © 2024 DiaperBazaar. All rights reserved.
      </td>
    </tr>
  </table>
</body>
</html>
""".formatted(userName);
    }

    // ===================== PASSWORD RESET EMAIL =====================

    private String buildPasswordResetEmailTemplate(String userName, String otpCode) {
        return """
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Password Reset</title>
</head>
<body style="font-family: Arial, sans-serif; background:#f4f6f8; padding:20px;">
  <table width="100%%" style="max-width:600px;margin:auto;background:#ffffff;border-radius:8px;">
    <tr>
      <td style="background:#6366f1;color:white;padding:30px;text-align:center;">
        <h1>🍼 DiaperBazaar</h1>
        <p>Password Reset</p>
      </td>
    </tr>
    <tr>
      <td style="padding:30px;">
        <h2>Hello %s,</h2>

        <p>Use the OTP below to reset your password:</p>

        <div style="background:#f3f4f6;
                    padding:20px;
                    text-align:center;
                    border-radius:6px;
                    margin:20px 0;">
          <span style="font-size:32px;
                       letter-spacing:6px;
                       color:#6366f1;
                       font-weight:bold;">
            %s
          </span>
        </div>

        <p style="color:red;">This OTP expires in 10 minutes.</p>

        <p>If you did not request this, please ignore this email.</p>
      </td>
    </tr>
    <tr>
      <td style="background:#1f2937;color:#9ca3af;padding:20px;text-align:center;">
        This is an automated email. Do not reply.
      </td>
    </tr>
  </table>
</body>
</html>
""".formatted(userName, otpCode);
    }
}
