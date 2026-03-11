package com.example.ecommerce_auth_service.config;

import com.example.ecommerce_auth_service.models.Role;
import com.example.ecommerce_auth_service.models.User;
import com.example.ecommerce_auth_service.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create USER if not exists
            if (!userRepository.existsByEmail("user@example.com")) {
                User user = User.builder()
                        .email("user@example.com")
                        .password(passwordEncoder.encode("user123"))
                        .role(Role.ROLE_USER)
                        .build();

                userRepository.save(user);
            }

            // Create ADMIN if not exists
            if (!userRepository.existsByEmail("admin@example.com")) {
                User admin = User.builder()
                        .email("admin@example.com")
                        .password(passwordEncoder.encode("admin123"))
                        .role(Role.ROLE_ADMIN)
                        .build();
                userRepository.save(admin);
            }
        };
    }
}