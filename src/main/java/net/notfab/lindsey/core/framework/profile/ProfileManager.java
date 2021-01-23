package net.notfab.lindsey.core.framework.profile;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public interface ProfileManager {

    @NotNull
    default MemberProfile get(Member member) {
        return this.getMember(member.getGuild().getIdLong(), member.getUser().getIdLong());
    }

    @NotNull
    MemberProfile getMember(long guildId, long userId);

    @NotNull
    default UserProfile getUser(Member member) {
        return this.get(member.getUser());
    }

    @NotNull
    default UserProfile get(User user) {
        return this.getUser(user.getIdLong());
    }

    @NotNull
    UserProfile getUser(long id);

    @NotNull
    default ServerProfile get(Guild guild) {
        return this.getGuild(guild.getIdLong());
    }

    @NotNull
    ServerProfile getGuild(long id);

    void save(@NotNull ServerProfile profile);

    void save(@NotNull UserProfile profile);

    void save(@NotNull MemberProfile profile);

}
