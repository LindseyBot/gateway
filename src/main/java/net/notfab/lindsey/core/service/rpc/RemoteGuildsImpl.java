package net.notfab.lindsey.core.service.rpc;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.shared.rpc.FGuild;
import net.notfab.lindsey.shared.rpc.FTextChannel;
import net.notfab.lindsey.shared.rpc.FVoiceChannel;
import net.notfab.lindsey.shared.rpc.services.RemoteGuilds;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class RemoteGuildsImpl implements RemoteGuilds {

    private final ShardManager shardManager;
    private final PermissionManager permissions;

    public RemoteGuildsImpl(ShardManager shardManager, PermissionManager permissions) {
        this.shardManager = shardManager;
        this.permissions = permissions;
    }

    private boolean hasPermission(Guild guild, long userId) {
        if (userId == 87166524837613568L) {
            return true;
        }
        Member member = guild.retrieveMemberById(userId)
            .complete();
        if (member == null) {
            return false;
        }
        return this.permissions.hasPermission(member, "ADMIN");
    }

    @Override
    public FGuild getGuild(long id, long userId) {
        Guild guild = this.shardManager.getGuildById(id);
        if (guild == null) {
            throw new IllegalArgumentException("Unknown guild");
        }
        if (!this.hasPermission(guild, userId)) {
            throw new IllegalStateException("No permission");
        }
        FGuild result = new FGuild();
        result.setId(id);
        result.setName(guild.getName());
        result.setIconUrl(guild.getIconUrl());
        result.setTextChannels(guild.getTextChannels().stream().map(ch -> {
            FTextChannel channel = new FTextChannel();
            channel.setId(ch.getIdLong());
            channel.setName(ch.getName());
            channel.setPosition(ch.getPosition());
            return channel;
        }).collect(Collectors.toList()));
        result.setVoiceChannels(guild.getVoiceChannels().stream().map(ch -> {
            FVoiceChannel channel = new FVoiceChannel();
            channel.setId(ch.getIdLong());
            channel.setName(ch.getName());
            channel.setPosition(ch.getPosition());
            return channel;
        }).collect(Collectors.toList()));
        return result;
    }

}
