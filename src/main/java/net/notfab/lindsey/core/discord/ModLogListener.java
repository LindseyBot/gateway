package net.notfab.lindsey.core.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.dv8tion.jda.api.audit.ActionType;
import net.dv8tion.jda.api.audit.AuditLogEntry;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildBanEvent;
import net.dv8tion.jda.api.events.guild.GuildUnbanEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.service.ModLogService;
import net.notfab.lindsey.shared.entities.ModLog;
import net.notfab.lindsey.shared.enums.ModLogType;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
public class ModLogListener extends ListenerAdapter implements ExpirationListener<Long, ModLogListener.ReferenceHolder> {

    private final Snowflake snowflake;
    private final ModLogService service;
    private final ShardManager shardManager;
    private final ExpiringMap<Long, ReferenceHolder> auditLogWait = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.CREATED)
        .expiration(5, TimeUnit.SECONDS)
        .asyncExpirationListener(this)
        .build();

    public ModLogListener(Lindsey lindsey, Snowflake snowflake, ModLogService service, ShardManager shardManager) {
        this.snowflake = snowflake;
        this.service = service;
        this.shardManager = shardManager;
        lindsey.addEventListener(this);
    }

    @Override
    public void onGuildBan(@NotNull GuildBanEvent event) {
        this.auditLogWait.put(event.getUser().getIdLong(),
            new ReferenceHolder(event.getGuild().getIdLong(), event.getUser().getName()));
    }

    @Override
    public void onGuildUnban(@NotNull GuildUnbanEvent event) {
        this.auditLogWait.put(event.getUser().getIdLong(),
            new ReferenceHolder(event.getGuild().getIdLong(), event.getUser().getName()));
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        this.auditLogWait.put(event.getUser().getIdLong(),
            new ReferenceHolder(event.getGuild().getIdLong(), event.getUser().getName()));
    }

    @Override
    public void expired(Long userId, ReferenceHolder reference) {
        Guild guild = this.shardManager.getGuildById(reference.getGuildId());
        if (guild == null) {
            return;
        }
        Optional<AuditLogEntry> oEntry = guild.retrieveAuditLogs()
            .user(userId).stream()
            .findFirst();
        if (oEntry.isEmpty()) {
            return;
        }
        AuditLogEntry entry = oEntry.get();
        if (entry.getUser() == null || entry.getUser().isBot()) {
            return;
        }
        ModLog log = new ModLog();
        log.setId(snowflake.next());
        log.setGuild(reference.getGuildId());
        log.setAdmin(entry.getUser().getIdLong());
        log.setTargetId(userId);
        log.setTargetName(reference.getUserName());
        log.setReason(entry.getReason());
        if (entry.getType() == ActionType.BAN) {
            log.setType(ModLogType.BAN);
        } else if (entry.getType() == ActionType.KICK) {
            log.setType(ModLogType.KICK);
        } else if (entry.getType() == ActionType.UNBAN) {
            log.setType(ModLogType.UNBAN);
        }
        this.service.save(log, guild);
    }

    @Data
    @AllArgsConstructor
    static class ReferenceHolder {

        private long guildId;
        private String userName;

    }

}
