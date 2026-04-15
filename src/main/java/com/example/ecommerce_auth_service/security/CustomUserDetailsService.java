package com.example.ecommerce_auth_service.security;

import com.example.ecommerce_auth_service.models.User;
import com.example.ecommerce_auth_service.repositories.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmailWithRoles(normalizedEmail).orElseThrow(
                () -> new UsernameNotFoundException("User not found with email: " + normalizedEmail) );
        return new CustomUserDetails(user);
    }

}