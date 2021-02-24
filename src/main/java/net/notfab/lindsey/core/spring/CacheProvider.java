package net.notfab.lindsey.core.spring;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.framework.models.BlackjackModel;
import net.notfab.lindsey.shared.entities.profile.MemberProfile;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
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

    @Bean
    ExpiringMap<Long, UserProfile> userProfileCache() {
        return ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(1, TimeUnit.MINUTES)
            .maxSize(10_000)
            .build();
    }

    @Bean
    ExpiringMap<Long, ServerProfile> serverProfileCache() {
        return ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(5, TimeUnit.MINUTES)
            .maxSize(50_000)
            .build();
    }

    @Bean
    ExpiringMap<String, MemberProfile> memberProfileCache() {
        return ExpiringMap.builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(5, TimeUnit.MINUTES)
            .maxSize(100_000)
            .build();
    }

}
