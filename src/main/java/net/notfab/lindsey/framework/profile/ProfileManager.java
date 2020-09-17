package net.notfab.lindsey.framework.profile;

import lombok.Getter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.framework.profile.repositories.ServerProfileRepository;
import net.notfab.lindsey.framework.profile.repositories.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProfileManager {

    @Getter
    private static ProfileManager Instance;

    private final UserProfileRepository userRepository;
    private final ServerProfileRepository guildRepository;

    public ProfileManager(UserProfileRepository userRepository, ServerProfileRepository guildRepository) {
        this.userRepository = userRepository;
        this.guildRepository = guildRepository;
        Instance = this;
    }

    public UserProfile get(Member member) {
        return this.get(member.getUser());
    }

    public UserProfile get(User user) {
        return this.getUser(user.getIdLong());
    }

    private UserProfile getUser(long id) {
        Optional<UserProfile> user = this.userRepository.findById(id);
        if (user.isEmpty()) {
            UserProfile settings = new UserProfile();
            settings.setOwner(id);
            return settings;
        } else {
            return user.get();
        }
    }

    public GuildProfile get(Guild guild) {
        return this.getGuild(guild.getIdLong());
    }

    public GuildProfile getGuild(long id) {
        Optional<GuildProfile> guild = this.guildRepository.findById(id);
        if (guild.isEmpty()) {
            GuildProfile settings = new GuildProfile();
            settings.setOwner(id);
            return settings;
        } else {
            return guild.get();
        }
    }

    public void save(GuildProfile profile) {
        guildRepository.save(profile);
    }

    public void save(UserProfile profile) {
        userRepository.save(profile);
    }

}
