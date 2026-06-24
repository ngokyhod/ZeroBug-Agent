package com.zerobug_agent.config;

import com.zerobug_agent.entity.User;
import com.zerobug_agent.entity.UserRole;
import com.zerobug_agent.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ZeroBugAgentProperties properties;

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    public DataSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder, ZeroBugAgentProperties properties) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public void run(String... args) {
        String adminEmail = properties.getAdmin().getEmail();
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = new User();
            admin.setEmail(adminEmail);
            admin.setPasswordHash(passwordEncoder.encode(properties.getAdmin().getPassword()));
            admin.setFullName(properties.getAdmin().getFullName());
            admin.setRole(UserRole.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Created default admin account: {}", adminEmail);
        }
    }
}
