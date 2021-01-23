package net.notfab.lindsey.core.framework.profile;

import lombok.Getter;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.repositories.mongo.MemberProfileRepository;
import net.notfab.lindsey.core.repositories.mongo.ServerProfileRepository;
import net.notfab.lindsey.core.repositories.mongo.UserProfileRepository;
import net.notfab.lindsey.shared.entities.profile.MemberProfile;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileManagerImpl implements ProfileManager, ExpirationListener<String, MemberProfile> {

    @Getter
    private static ProfileManagerImpl Instance;

    private final UserProfileRepository userRepository;
    private final ServerProfileRepository serverRepository;
    private final MemberProfileRepository memberRepository;

    private final ExpiringMap<Long, UserProfile> userProfileCache;
    private final ExpiringMap<Long, ServerProfile> serverProfileCache;
    private final ExpiringMap<String, MemberProfile> memberProfileCache;

    public ProfileManagerImpl(UserProfileRepository userRepository,
                              ServerProfileRepository serverRepository,
                              MemberProfileRepository memberRepository,
                              ExpiringMap<Long, UserProfile> userProfileCache,
                              ExpiringMap<Long, ServerProfile> serverProfileCache,
                              ExpiringMap<String, MemberProfile> memberProfileCache) {
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.memberRepository = memberRepository;
        this.userProfileCache = userProfileCache;
        this.serverProfileCache = serverProfileCache;
        this.memberProfileCache = memberProfileCache;
        this.memberProfileCache.addExpirationListener(this);
        Instance = this;
    }

    @Override
    public @NotNull MemberProfile getMember(long guild, long user) {
        String id = guild + ":" + user;
        if (this.memberProfileCache.containsKey(id)) {
            return this.memberProfileCache.get(id);
        }
        Optional<MemberProfile> oProfile = memberRepository.findById(id);
        MemberProfile profile;
        if (oProfile.isEmpty()) {
            profile = new MemberProfile();
            profile.setId(id);
            profile.setGuildId(guild);
            profile.setUserId(user);
        } else {
            profile = oProfile.get();
            this.memberProfileCache.put(id, profile);
        }
        return profile;
    }

    @Override
    public @NotNull UserProfile getUser(long id) {
        if (this.userProfileCache.containsKey(id)) {
            return this.userProfileCache.get(id);
        }
        Optional<UserProfile> oProfile = this.userRepository.findById(String.valueOf(id));
        UserProfile profile;
        if (oProfile.isEmpty()) {
            profile = new UserProfile();
            profile.setId(String.valueOf(id));
        } else {
            profile = oProfile.get();
            this.userProfileCache.put(id, profile);
        }
        return profile;
    }

    @Override
    public @NotNull ServerProfile getGuild(long id) {
        if (this.serverProfileCache.containsKey(id)) {
            return this.serverProfileCache.get(id);
        }
        Optional<ServerProfile> oProfile = this.serverRepository.findById(String.valueOf(id));
        ServerProfile profile;
        if (oProfile.isEmpty()) {
            profile = new ServerProfile();
            profile.setId(String.valueOf(id));
        } else {
            profile = oProfile.get();
            this.serverProfileCache.put(id, profile);
        }
        return profile;
    }

    @Override
    public void save(@NotNull ServerProfile profile) {
        serverRepository.save(profile);
        this.serverProfileCache.remove(Long.parseLong(profile.getId()));
    }

    @Override
    public void save(@NotNull UserProfile profile) {
        userRepository.save(profile);
        this.userProfileCache.remove(Long.parseLong(profile.getId()));
    }

    @Override
    public void save(@NotNull MemberProfile profile) {
        // No-op - saved on expire
        if (!this.memberProfileCache.containsKey(profile.getId())) {
            this.memberProfileCache.put(profile.getId(), profile);
        }
    }

    @Override
    public void expired(String key, MemberProfile value) {
        this.memberRepository.save(value);
    }

}
