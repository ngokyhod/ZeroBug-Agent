package com.zerobug_agent.service;

import com.zerobug_agent.config.ZeroBugAgentProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final ZeroBugAgentProperties properties;

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    public EmailService(JavaMailSender mailSender, ZeroBugAgentProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        if (properties.getMail().isDevMode()) {
            log.info("=== DEV MODE: Password Reset Link ===");
            log.info("To: {}", to);
            log.info("Reset link: {}", resetLink);
            log.info("=====================================");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("ZeroBug Agent - Đặt lại mật khẩu");
        message.setText("Bạn đã yêu cầu đặt lại mật khẩu.\n\n"
                + "Nhấn vào link sau để đặt lại mật khẩu (hết hạn sau 1 giờ):\n"
                + resetLink + "\n\n"
                + "Nếu bạn không yêu cầu, hãy bỏ qua email này.");
        mailSender.send(message);
    }
}
