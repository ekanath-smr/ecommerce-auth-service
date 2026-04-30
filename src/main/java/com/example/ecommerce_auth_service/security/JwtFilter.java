package com.example.ecommerce_auth_service.security;

import com.example.ecommerce_auth_service.services.TokenBlacklistService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtFilter.class);

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtFilter(JwtService jwtService, UserDetailsService userDetailsService, TokenBlacklistService tokenBlacklistService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().startsWith("/auth");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);

        try {
            if (tokenBlacklistService.isBlacklisted(jwt)) {
                logger.warn("Blacklisted token used");
                filterChain.doFilter(request, response);
                return;
            }

            String tokenType = jwtService.extractTokenType(jwt);
            if (!"ACCESS".equals(tokenType)) {
                logger.warn("Invalid token type | method={} | path={}", method, path);
                filterChain.doFilter(request, response);
                return;
            }

            String username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                var userDetails = userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    var authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("JWT validated for user {}", username);
                }
            }

        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("Invalid JWT received");
        }

        filterChain.doFilter(request, response);
    }
}