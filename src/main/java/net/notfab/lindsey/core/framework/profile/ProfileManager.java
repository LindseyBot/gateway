package net.notfab.lindsey.core.framework.profile;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.repositories.mongo.MemberProfileRepository;
import net.notfab.lindsey.core.repositories.mongo.ServerProfileRepository;
import net.notfab.lindsey.core.repositories.mongo.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileManager {

    @Getter
    private static ProfileManager Instance;

    private final UserProfileRepository userRepository;
    private final ServerProfileRepository guildRepository;
    private final MemberProfileRepository memberRepository;

    public ProfileManager(UserProfileRepository userRepository, ServerProfileRepository guildRepository,
                          MemberProfileRepository memberRepository) {
        this.userRepository = userRepository;
        this.guildRepository = guildRepository;
        this.memberRepository = memberRepository;
        Instance = this;
    }

    public MemberProfile get(Member member) {
        return this.getMember(member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    public MemberProfile getMember(long guild, long user) {
        Optional<MemberProfile> oProfile = memberRepository.findById(guild + ":" + user);
        if (oProfile.isEmpty()) {
            MemberProfile profile = new MemberProfile();
            profile.setId(guild + ":" + user);
            profile.setGuildId(guild);
            profile.setUserId(user);
            return profile;
        } else {
            return oProfile.get();
        }
    }

    public UserProfile getUser(Member member) {
        return this.get(member.getUser());
    }

    public UserProfile get(User user) {
        return this.getUser(user.getIdLong());
    }

    public UserProfile getUser(long id) {
        Optional<UserProfile> user = this.userRepository.findById(String.valueOf(id));
        if (user.isEmpty()) {
            UserProfile settings = new UserProfile();
            settings.setOwner(String.valueOf(id));
            return settings;
        } else {
            return user.get();
        }
    }

    public ServerProfile get(Guild guild) {
        return this.getGuild(guild.getIdLong());
    }

    public ServerProfile getGuild(long id) {
        Optional<ServerProfile> guild = this.guildRepository.findById(String.valueOf(id));
        if (guild.isEmpty()) {
            ServerProfile settings = new ServerProfile();
            settings.setOwner(String.valueOf(id));
            return settings;
        } else {
            return guild.get();
        }
    }

    public void save(ServerProfile profile) {
        guildRepository.save(profile);
    }

    public void save(UserProfile profile) {
        userRepository.save(profile);
    }

    public void save(MemberProfile profile) {
        memberRepository.save(profile);
    }

}
