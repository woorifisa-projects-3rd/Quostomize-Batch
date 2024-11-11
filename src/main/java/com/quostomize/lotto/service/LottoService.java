package com.quostomize.lotto.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class LottoService {

    private final RedisTemplate<String, String> redisTemplate;

    // Redis에 데이터 저장
    public void saveData(String key, String value) {
        // 60초 동안 데이터 유지

        redisTemplate.opsForValue().set(key, value, 60, TimeUnit.SECONDS);
    }

    // Redis에서 데이터 조회
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}
