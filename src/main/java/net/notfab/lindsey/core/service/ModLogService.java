package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.ModLog;
import net.notfab.lindsey.shared.enums.ModLogType;
import net.notfab.lindsey.shared.repositories.sql.ModLogRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.springframework.stereotype.Service;

@Service
public class ModLogService {

    private final Snowflake snowflake;
    private final ModLogRepository repository;
    private final ProfileManager profiles;

    public ModLogService(Snowflake snowflake, ModLogRepository repository, ProfileManager profiles) {
        this.snowflake = snowflake;
        this.repository = repository;
        this.profiles = profiles;
    }

    public boolean isEnabled(Guild guild) {
        return this.profiles.get(guild).isModLogEnabled();
    }

    public void ban(Member target, long admin, String reason) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.BAN);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getEffectiveName());
        modLog.setReason(reason);
        this.repository.save(modLog);
    }

    public void softban(Member target, long admin, String reason) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.SOFTBAN);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getEffectiveName());
        modLog.setReason(reason);
        this.repository.save(modLog);
    }

    public void hackban(Guild guild, long userId, long admin, String reason) {
        if (!this.isEnabled(guild)) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(guild.getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.BAN);
        modLog.setTargetId(userId);
        modLog.setTargetName(null);
        modLog.setReason(reason);
        this.repository.save(modLog);
    }

    public void unban(Member target, long admin, String reason) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.UNBAN);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getEffectiveName());
        modLog.setReason(reason);
        this.repository.save(modLog);
    }

    public void kick(Member target, long admin, String reason) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.KICK);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getEffectiveName());
        modLog.setReason(reason);
        this.repository.save(modLog);
    }

    public void mute(Member target, long admin, long durationMs, String reason) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.BAN);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getEffectiveName());
        modLog.setDuration(durationMs);
        modLog.setReason(reason);
        this.repository.save(modLog);
    }

    public void warn(Member target, long admin, String reason) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.WARN);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getEffectiveName());
        modLog.setReason(reason);
        this.repository.save(modLog);
    }

    public void prune(TextChannel target, long admin, int size) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.WARN);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getName());
        modLog.setReason("Deleted " + size + " messages.");
        this.repository.save(modLog);
    }

    public void slowmode(TextChannel target, long admin, int seconds) {
        if (!this.isEnabled(target.getGuild())) {
            return;
        }
        ModLog modLog = new ModLog();
        modLog.setId(snowflake.next());
        modLog.setGuild(target.getGuild().getIdLong());
        modLog.setAdmin(admin);
        modLog.setType(ModLogType.WARN);
        modLog.setTargetId(target.getIdLong());
        modLog.setTargetName(target.getName());
        modLog.setReason(seconds > 0 ? "Enabled (" + seconds + "s)" : "Disabled");
        this.repository.save(modLog);
    }

    public void save(ModLog log, Guild guild) {
        if (!this.isEnabled(guild)) {
            return;
        }
        this.repository.save(log);
    }

}
