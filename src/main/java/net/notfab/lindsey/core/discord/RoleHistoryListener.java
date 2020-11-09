package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.options.Option;
import net.notfab.lindsey.framework.options.OptionManager;
import net.notfab.lindsey.framework.profile.MemberProfile;
import net.notfab.lindsey.framework.profile.ProfileManager;
import net.notfab.lindsey.framework.profile.member.RoleHistory;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleHistoryListener extends ListenerAdapter {

    private final ProfileManager profiles;
    private final OptionManager options;
    private final Translator i18n;

    public RoleHistoryListener(Lindsey lindsey, ProfileManager profiles, OptionManager options, Translator i18n) {
        lindsey.addEventListener(this);
        this.profiles = profiles;
        this.options = options;
        this.i18n = i18n;
    }

    @Override
    public void onGuildMemberRoleAdd(@NotNull GuildMemberRoleAddEvent event) {
        this.saveRoles(event.getMember());
    }

    @Override
    public void onGuildMemberRoleRemove(@NotNull GuildMemberRoleRemoveEvent event) {
        this.saveRoles(event.getMember());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (event.getMember() == null) {
            return;
        }
        this.saveRoles(event.getMember());
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        Option option = this.options.find("keeproles");
        boolean isToKeepRoles = this.options.get(option, event.getGuild());
        if (!isToKeepRoles) {
            return;
        }
        MemberProfile profile = profiles.get(event.getMember());
        if (profile.getRoleHistory() == null) {
            return;
        }
        Set<Role> roles = profile.getRoleHistory().getRoles()
            .stream()
            .map(roleId -> event.getGuild().getRoleById(roleId))
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        event.getGuild().modifyMemberRoles(event.getMember(), roles)
            .reason(i18n.get(event.getGuild(), "features.keeproles", roles.size()))
            .queue();
    }

    private void saveRoles(Member member) {
        Set<String> roles = member.getRoles()
            .stream()
            .map(ISnowflake::getId)
            .collect(Collectors.toSet());
        MemberProfile profile = profiles.get(member);
        RoleHistory history = profile.getRoleHistory();
        history.getRoles().clear();
        history.getRoles().addAll(roles);
        history.setLastUpdated(System.currentTimeMillis());
        profiles.save(profile);
    }

}
