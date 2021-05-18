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
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.service.KeepRoleService;
import net.notfab.lindsey.shared.entities.profile.member.RoleHistory;
import net.notfab.lindsey.shared.repositories.sql.RoleHistoryRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleHistoryListener extends ListenerAdapter {

    private final Snowflake snowflake;
    private final Translator i18n;
    private final KeepRoleService keepRoleService;
    private final RoleHistoryRepository repository;

    public RoleHistoryListener(Lindsey lindsey, Snowflake snowflake, Translator i18n,
                               KeepRoleService keepRoleService, RoleHistoryRepository repository) {
        this.snowflake = snowflake;
        this.repository = repository;
        this.i18n = i18n;
        this.keepRoleService = keepRoleService;
        lindsey.addEventListener(this);
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
        boolean isToKeepRoles = this.keepRoleService.isActive(event.getGuild());
        if (!isToKeepRoles) {
            return;
        }
        Optional<RoleHistory> oHistory = this.repository
            .findByUserAndGuild(event.getUser().getIdLong(), event.getGuild().getIdLong());
        if (oHistory.isEmpty()) {
            return;
        }
        Set<Role> roles = oHistory.get().getRoles()
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
        Optional<RoleHistory> oHistory = this.repository
            .findByUserAndGuild(member.getUser().getIdLong(), member.getGuild().getIdLong());
        RoleHistory history;
        if (oHistory.isPresent()) {
            history = oHistory.get();
        } else {
            history = new RoleHistory();
            history.setId(this.snowflake.next());
            history.setUser(member.getUser().getIdLong());
            history.setGuild(member.getGuild().getIdLong());
        }
        if (history.getRoles() == null) {
            history.setRoles(new HashSet<>());
        } else {
            history.getRoles().clear();
        }
        history.getRoles().addAll(roles);
        history.setLastUpdated(System.currentTimeMillis());
        this.repository.save(history);
    }

}
