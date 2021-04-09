package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import org.springframework.stereotype.Service;

@Service
public class KeepRoleService {

    private final ProfileManager profiles;

    public KeepRoleService(ProfileManager profiles) {
        this.profiles = profiles;
    }

    public boolean isActive(Guild guild) {
        return this.isActive(guild.getIdLong());
    }

    public boolean isActive(long guild) {
        ServerProfile profile = profiles.getGuild(guild);
        return false;
    }

}
