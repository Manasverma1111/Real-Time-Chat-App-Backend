package com.microservices.authservice.service;

public interface TokenBlacklistService {

//    This interface defines the contract for a token blacklist service,
//    which is responsible for managing blacklisted JWT tokens.
    void blacklistToken(String token, long expiry);

//    The blacklistToken() method takes a token and its expiry time,
//    and is responsible for adding the token to the blacklist,
//    ensuring that it cannot be used for authentication until it expires.
    boolean isBlacklisted(String token);
}