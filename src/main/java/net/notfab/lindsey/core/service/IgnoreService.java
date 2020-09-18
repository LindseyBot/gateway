package net.notfab.lindsey.core.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class IgnoreService {

    private final StringRedisTemplate redis;

    public IgnoreService(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @SuppressWarnings("ConstantConditions")
    public boolean isIgnored(long guild, long channel) {
        if (!redis.hasKey("Lindsey:Ignore:" + guild)) {
            return false;
        }
        return redis.opsForSet().isMember("Lindsey:Ignore:" + guild, String.valueOf(channel));
    }

    public void remove(long guild, long channel) {
        redis.opsForSet().remove("Lindsey:Ignore:" + guild, String.valueOf(channel));
    }

    public void add(long guild, long channel) {
        redis.opsForSet().add("Lindsey:Ignore:" + guild, String.valueOf(channel));
    }

    public Set<String> getAll(long guild) {
        return redis.opsForSet().members("Lindsey:Ignore:" + guild);
    }

}
