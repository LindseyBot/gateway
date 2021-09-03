package net.notfab.lindsey.core.framework.profile;

import lombok.Getter;
import net.notfab.lindsey.core.repositories.sql.MemberProfileRepository;
import net.notfab.lindsey.core.repositories.sql.ServerProfileRepository;
import net.notfab.lindsey.core.repositories.sql.UserProfileRepository;
import net.notfab.lindsey.shared.entities.profile.MemberProfile;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
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

    public ProfileManagerImpl(
        Snowflake snowflake,
        UserProfileRepository userRepository,
        ServerProfileRepository serverRepository,
        MemberProfileRepository memberRepository
    ) {
        this.snowflake = snowflake;
        this.userRepository = userRepository;
        this.serverRepository = serverRepository;
        this.memberRepository = memberRepository;
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
        Optional<UserProfile> oProfile = this.userRepository.findById(id);
        UserProfile profile;
        if (oProfile.isEmpty()) {
            profile = new UserProfile();
            profile.setUser(id);
        } else {
            profile = oProfile.get();
        }
        return profile;
    }

    @Override
    public @NotNull ServerProfile getGuild(long id) {
        Optional<ServerProfile> oProfile = this.serverRepository.findById(id);
        ServerProfile profile;
        if (oProfile.isEmpty()) {
            profile = new ServerProfile();
            profile.setGuild(id);
        } else {
            profile = oProfile.get();
        }
        return profile;
    }

    @Override
    public void save(@NotNull ServerProfile profile) {
        serverRepository.save(profile);
    }

    @Override
    public void save(@NotNull UserProfile profile) {
        userRepository.save(profile);
    }

    @Override
    public void save(@NotNull MemberProfile profile) {
        memberRepository.save(profile);
    }

}
