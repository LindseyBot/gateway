package net.notfab.lindsey.core.framework.profile;

import lombok.Getter;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.shared.entities.profile.MemberProfile;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import net.notfab.lindsey.shared.repositories.sql.MemberProfileRepository;
import net.notfab.lindsey.shared.repositories.sql.ServerProfileRepository;
import net.notfab.lindsey.shared.repositories.sql.UserProfileRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileManagerImpl implements ProfileManager {

    @Getter
    private static ProfileManagerImpl Instance;

    private final Snowflake snowflake;
    private final UserProfileRepository userRepository;
    private final ServerProfileRepository serverRepository;
    private final MemberProfileRepository memberRepository;

    private final ExpiringMap<Long, UserProfile> userProfileCache;
    private final ExpiringMap<Long, ServerProfile> serverProfileCache;

    public ProfileManagerImpl(Snowflake snowflake, UserProfileRepository userRepository,
                              ServerProfileRepository serverRepository,
                              MemberProfileRepository memberRepository,
                              ExpiringMap<Long, UserProfile> userProfileCache,
                              ExpiringMap<Long, ServerProfile> serverProfileCache) {
        this.snowflake = snowflake;
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.memberRepository = memberRepository;
        this.userProfileCache = userProfileCache;
        this.serverProfileCache = serverProfileCache;
        Instance = this;
    }

    @Override
    public @NotNull MemberProfile getMember(long guild, long user) {
        Optional<MemberProfile> oProfile = memberRepository.findByUserAndGuild(user, guild);
        MemberProfile profile;
        if (oProfile.isEmpty()) {
            profile = new MemberProfile();
            profile.setId(this.snowflake.next());
            profile.setGuild(guild);
            profile.setUser(user);
            profile.setLastSeen(System.currentTimeMillis());
        } else {
            profile = oProfile.get();
        }
        return profile;
    }

    @Override
    public @NotNull UserProfile getUser(long id) {
        if (this.userProfileCache.containsKey(id)) {
            return this.userProfileCache.get(id);
        }
        Optional<UserProfile> oProfile = this.userRepository.findById(id);
        UserProfile profile;
        if (oProfile.isEmpty()) {
            profile = new UserProfile();
            profile.setUser(id);
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
        Optional<ServerProfile> oProfile = this.serverRepository.findById(id);
        ServerProfile profile;
        if (oProfile.isEmpty()) {
            profile = new ServerProfile();
            profile.setGuild(id);
        } else {
            profile = oProfile.get();
            this.serverProfileCache.put(id, profile);
        }
        return profile;
    }

    @Override
    public void save(@NotNull ServerProfile profile) {
        serverRepository.save(profile);
        this.serverProfileCache.remove(profile.getGuild());
    }

    @Override
    public void save(@NotNull UserProfile profile) {
        userRepository.save(profile);
        this.userProfileCache.remove(profile.getUser());
    }

    @Override
    public void save(@NotNull MemberProfile profile) {
        memberRepository.save(profile);
    }

}
