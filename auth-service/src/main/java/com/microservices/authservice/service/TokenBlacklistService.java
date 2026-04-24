package com.microservices.authservice.service;

public interface TokenBlacklistService {

    void blacklistToken(String token, long expiry);

    boolean isBlacklisted(String token);
}