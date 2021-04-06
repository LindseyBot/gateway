package net.notfab.lindsey.core.service.rpc;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.shared.rpc.*;
import net.notfab.lindsey.shared.rpc.services.RemoteGuilds;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

        List<FRole> roles = guild.getRoles().stream()
            .map(role -> {
                FRole fRole = new FRole();
                fRole.setId(role.getIdLong());
                fRole.setName(role.getName());
                fRole.setPosition(role.getPosition());
                return fRole;
            }).collect(Collectors.toList());
        result.setRoles(roles);

        List<GuildChannel> topChannels = guild.getChannels().stream()
            .filter(channel -> channel.getParent() == null)
            .collect(Collectors.toList());
        result.setChannels(this.parseChannels(topChannels));
        return result;
    }

    @Override
    public List<FGuild> getDetails(List<Long> ids, long userId) {
        List<FGuild> guilds = new ArrayList<>();
        ids.parallelStream().forEach(id -> {
            FGuild guild;
            try {
                guild = this.getGuild(id, userId);
            } catch (IllegalStateException | IllegalArgumentException ex) {
                return;
            }
            guilds.add(guild);
        });
        return guilds;
    }

    private List<FChannel> parseCategory(Category category, int pos) {
        List<FChannel> channels = new ArrayList<>();
        for (GuildChannel gChannel : category.getChannels()) {
            FChannel fChannel = new FChannel();
            fChannel.setId(gChannel.getIdLong());
            fChannel.setName(gChannel.getName());
            fChannel.setPosition(pos++);
            if (gChannel.getType() == ChannelType.STORE) {
                fChannel.setType(FChannelType.STORE);
            } else if (gChannel.getType() == ChannelType.VOICE) {
                fChannel.setType(FChannelType.VOICE);
            } else {
                fChannel.setType(FChannelType.TEXT);
            }
            channels.add(fChannel);
        }
        return channels;
    }

    private List<FChannel> parseChannels(List<GuildChannel> guildChannels) {
        List<FChannel> channels = new ArrayList<>();
        int pos = 0;
        for (GuildChannel gChannel : guildChannels) {
            if (gChannel instanceof Category) {
                FCategory fCategory = new FCategory();
                fCategory.setId(gChannel.getIdLong());
                fCategory.setName(gChannel.getName());
                fCategory.setPosition(pos);
                fCategory.setType(FChannelType.CATEGORY);
                fCategory.setChannels(this.parseCategory((Category) gChannel, pos + 1));
                channels.add(fCategory);
                pos = pos + (fCategory.getChannels().size() + 1);
            } else {
                FChannel fChannel = new FChannel();
                fChannel.setId(gChannel.getIdLong());
                fChannel.setName(gChannel.getName());
                fChannel.setPosition(pos);
                if (gChannel.getType() == ChannelType.STORE) {
                    fChannel.setType(FChannelType.STORE);
                } else if (gChannel.getType() == ChannelType.VOICE) {
                    fChannel.setType(FChannelType.VOICE);
                } else {
                    fChannel.setType(FChannelType.TEXT);
                }
                channels.add(fChannel);
                pos++;
            }
        }
        return channels;
    }

}
