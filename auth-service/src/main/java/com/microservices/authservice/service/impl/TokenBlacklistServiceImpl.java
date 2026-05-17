package com.microservices.authservice.service.impl;

import com.microservices.authservice.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

//    This class implements the TokenBlacklistService interface,
//    providing methods to blacklist JWT tokens and check if a token is blacklisted.
    private final StringRedisTemplate stringRedisTemplate;

//    The blacklistToken() method takes a token and its expiry time,
//    and stores the token in Redis with a value of "BLACKLISTED" and an expiration time equal to the token's expiry.
    @Override
    public void blacklistToken(String token, long expiry) {
        stringRedisTemplate.opsForValue()
                .set(token, "BLACKLISTED", expiry, TimeUnit.MILLISECONDS);
    }

//    The isBlacklisted() method checks if a token exists in Redis,
//    returning true if the token is found (indicating it is blacklisted) and false otherwise.
    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(token)
        );
    }
}