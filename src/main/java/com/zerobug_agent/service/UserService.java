package com.zerobug_agent.service;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import com.zerobug_agent.dto.ForgotPasswordRequest;
import com.zerobug_agent.dto.RegisterRequest;
import com.zerobug_agent.dto.ResetPasswordRequest;
import com.zerobug_agent.entity.PasswordResetToken;
import com.zerobug_agent.entity.User;
import com.zerobug_agent.entity.UserRole;
import com.zerobug_agent.repository.PasswordResetTokenRepository;
import com.zerobug_agent.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final ZeroBugAgentProperties properties;

    public UserService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder, EmailService emailService,
                       ZeroBugAgentProperties properties) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.properties = properties;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (!email.endsWith("@gmail.com")) {
            throw new IllegalArgumentException("Chỉ chấp nhận đăng ký bằng tài khoản Gmail (@gmail.com)");
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã được đăng ký");
        }

        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(UserRole.USER)
                .enabled(true)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public void requestPasswordReset(ForgotPasswordRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        // Luôn trả về thành công để tránh lộ thông tin email có tồn tại trong hệ thống hay không.
        // Chỉ thực hiện hành động nếu tìm thấy user.
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .used(false)
                    .build();
            tokenRepository.save(resetToken);
            String resetLink = properties.getApp().getBaseUrl() + "/#/reset-password?token=" + token;
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("Token đã được sử dụng");
        }
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        resetToken.setUsed(true);
        userRepository.save(user);
        tokenRepository.save(resetToken);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
    }
}
