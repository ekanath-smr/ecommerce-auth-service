package com.example.ecommerce_auth_service.config;

import com.example.ecommerce_auth_service.models.Role;
import com.example.ecommerce_auth_service.models.User;
import com.example.ecommerce_auth_service.repositories.RoleRepository;
import com.example.ecommerce_auth_service.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initUsers(UserRepository userRepository,
                                RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 1. Create roles if not exist
            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_USER")));

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ROLE_ADMIN")));

            // 2. Create USER
            if (!userRepository.existsByEmail("user@gmail.com")) {
                User user = User.builder()
                        .email("user@gmail.com")
                        .password(passwordEncoder.encode("user123"))
                        .roles(Set.of(userRole))
                        .build();
                userRepository.save(user);
            }

            // 3. Create ADMIN
            if (!userRepository.existsByEmail("admin@gmail.com")) {
                User admin = User.builder()
                        .email("admin@gmail.com")
                        .password(passwordEncoder.encode("admin123"))
                        .roles(Set.of(adminRole))
                        .build();
                userRepository.save(admin);
            }
        };
    }
}