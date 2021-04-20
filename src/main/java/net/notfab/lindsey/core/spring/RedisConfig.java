package net.notfab.lindsey.core.spring;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.TimeUnit;

@Configuration
public class RedisConfig {

    @Bean
    @Primary
    RedisConnectionFactory redisFactory(RedisProperties properties) {
        RedisStandaloneConfiguration config =
            new RedisStandaloneConfiguration(properties.getHost(), properties.getPort());
        if (properties.getPassword() != null && !properties.getPassword().isBlank()) {
            config.setPassword(properties.getPassword());
        }
        return new JedisConnectionFactory(config);
    }

    @Bean
    RedisConnection redisConnection(RedisConnectionFactory factory) {
        return factory.getConnection();
    }

    @Bean
    public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<?, ?> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        return template;
    }

    @Bean
    @Primary
    public RedisProperties redisProperties() {
        return new RedisProperties();
    }

    @Bean
    public JedisPool jedisPool(RedisProperties config) {
        JedisPool pool;
        if (config.getPassword() != null && !config.getPassword().isBlank()) {
            pool = new JedisPool(new RedisPoolConfig(), config.getHost(), config.getPort(), 2000, config.getPassword());
        } else {
            pool = new JedisPool(new RedisPoolConfig(), config.getHost(), config.getPort(), 2000);
        }
        return pool;
    }

    private static class RedisPoolConfig extends GenericObjectPoolConfig<Jedis> {
        public RedisPoolConfig() {
            setTestWhileIdle(true);
            setMinEvictableIdleTimeMillis(60000);
            setTimeBetweenEvictionRunsMillis(30000);
            setNumTestsPerEvictionRun(-1);
            setMaxWaitMillis(TimeUnit.SECONDS.toMillis(30));
            setMaxTotal(1000);
        }
    }

}
