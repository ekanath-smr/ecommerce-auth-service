package com.example.ecommerce_auth_service.services;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// “Currently I use in-memory blacklist for simplicity,
// but in production I would use Redis with TTL to ensure scalability and automatic expiration.”
@Service
public class TokenBlacklistService {

    private final Map<String, Long> blacklistedTokens = new ConcurrentHashMap<>();

    public void blacklist(String token, long expirationTime) {
        blacklistedTokens.put(token, expirationTime);
    }

    public boolean isBlacklisted(String token) {
        Long expiry = blacklistedTokens.get(token);
        if (expiry == null) return false;
        if (expiry < System.currentTimeMillis()) {
            blacklistedTokens.remove(token); // cleanup
            return false;
        }
        return true;
    }

    public void remove(String token) {
        blacklistedTokens.remove(token);
    }

    public int size() {
        return blacklistedTokens.size();
    }
}
