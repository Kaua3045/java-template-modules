package com.kaua.template.infrastructure.configurations;

import com.kaua.template.infrastructure.idempotency.gateways.IdempotencyKeyGateway;
import com.kaua.template.infrastructure.idempotency.gateways.InMemoryIdempotencyKeyGateway;
import com.kaua.template.infrastructure.idempotency.gateways.RedisIdempotencyKeyGateway;
import com.kaua.template.infrastructure.utils.ObservationHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestClient;

@Configuration(proxyBeanMethods = false)
public class IdempotencyKeyConfig {

    @Bean
    @ConditionalOnProperty(name = "idempotency-key.storage.type", havingValue = "in-memory")
    public IdempotencyKeyGateway inMemoryIdempotencyKeyGateway(final ObservationHelper observationHelper) {
        return new InMemoryIdempotencyKeyGateway(observationHelper);
    }

    @Bean
    @ConditionalOnProperty(name = "idempotency-key.storage.type", havingValue = "redis")
    public IdempotencyKeyGateway redisIdempotencyKeyGateway(final RedisTemplate<String, byte[]> redisTemplate, final ObservationHelper observationHelper) {
        return new RedisIdempotencyKeyGateway(redisTemplate, observationHelper);
    }
}
