package com.ens.servise;

import com.ens.models.UserData;
import com.ens.models.UserGroups;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class CacheService {

    private static final int TTL = 300;
    private final JedisPool jedisPool;
    private final ObjectMapper mapper;


    public Optional<UserData> getCachedUserData(Long chatId) {
        log.info("Getting cached user data for chatId {} in CacheService", chatId);
        try (Jedis jedis = jedisPool.getResource()) {
            String key = "userData:%d".formatted(chatId);
            String raw = jedis.get(key);

            if (raw != null) {
                log.info("user found in getCachedDateOfBirth: {}", raw);
                return Optional.ofNullable(mapper.readValue(raw, UserData.class));
            }

            log.info("user not found in cache by chatId: {}", key);
            return Optional.empty();
        } catch (JsonProcessingException e) {
            log.error("Error while parse UserData: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public void cacheUserData(Long chatId, UserData userData) {
        log.info("Method cacheUserData called with chatId: {}", chatId);

        try (Jedis jedis = jedisPool.getResource()) {
            String key = "userData:%d".formatted(chatId);

            jedis.setex(key, TTL, mapper.writeValueAsString(userData));
        } catch (JsonProcessingException e) {
            log.error("Error while serializing UserData: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
