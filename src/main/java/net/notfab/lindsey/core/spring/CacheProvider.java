package net.notfab.lindsey.core.spring;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.framework.models.BlackjackModel;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheProvider {

    @Bean
    ExpiringMap<Long, BlackjackModel> blackJackCache() {
        return ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(1, TimeUnit.MINUTES)
            .build();
    }

}
