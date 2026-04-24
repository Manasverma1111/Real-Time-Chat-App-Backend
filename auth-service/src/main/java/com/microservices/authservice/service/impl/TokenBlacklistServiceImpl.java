package com.microservices.authservice.service.impl;

import com.microservices.authservice.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void blacklistToken(String token, long expiry) {
        stringRedisTemplate.opsForValue()
                .set(token, "BLACKLISTED", expiry, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(
                stringRedisTemplate.hasKey(token)
        );
    }
}