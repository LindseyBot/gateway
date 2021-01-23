package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.service.ReactionRoleService;
import net.notfab.lindsey.shared.entities.ReactionRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReactionRoleListener extends ListenerAdapter {

    private final StringRedisTemplate redis;
    private final ReactionRoleService service;

    public ReactionRoleListener(Lindsey lindsey, StringRedisTemplate redis, ReactionRoleService service) {
        lindsey.addEventListener(this);
        this.redis = redis;
        this.service = service;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        this.createReactionRole(event);
        List<ReactionRole> roleList = service.findAll(event.getGuild());
        if (roleList.isEmpty()) {
            return;
        }
        roleList.stream()
            .filter(reactionRole -> event.getMessageIdLong() == reactionRole.getMessageId())
            .filter(reactionRole -> reactionRole.getReaction().equals(event.getReactionEmote().getAsReactionCode()))
            .forEach(reactionRole -> {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(reactionRole.getRoleId());
                if (role == null) {
                    service.remove(guild, reactionRole.getName());
                    return;
                }
                guild.addRoleToMember(event.getMember(), role)
                    .reason("Reaction Roles (" + reactionRole.getName() + ")")
                    .queue();
            });
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        List<ReactionRole> roleList = service.findAll(event.getGuild());
        if (roleList.isEmpty()) {
            return;
        }
        roleList.stream()
            .filter(reactionRole -> event.getMessageIdLong() == reactionRole.getMessageId())
            .filter(reactionRole -> reactionRole.getReaction().equals(event.getReactionEmote().getAsReactionCode()))
            .forEach(reactionRole -> {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(reactionRole.getRoleId());
                if (role == null) {
                    service.remove(guild, reactionRole.getName());
                    return;
                }
                guild.removeRoleFromMember(event.getUserId(), role)
                    .reason("Reaction Roles (" + reactionRole.getName() + ")")
                    .queue();
            });
    }

    public void createReactionRole(GuildMessageReactionAddEvent event) {
        String id = "ReactionRole:Creation:" + event.getGuild().getId();
        Boolean hasKey = redis.hasKey(id);
        if (hasKey == null || !hasKey) {
            return;
        }
        String member = (String) redis.opsForHash().get(id, "member");
        if (!event.getMember().getId().equals(member)) {
            return;
        }
        String roleId = (String) redis.opsForHash().get(id, "role");
        if (roleId == null) {
            return;
        }
        String responseChannelId = (String) redis.opsForHash().get(id, "channel");
        if (responseChannelId == null) {
            return;
        }
        String name = (String) redis.opsForHash().get(id, "name");
        if (name == null) {
            return;
        }
        ReactionRole reactionRole = new ReactionRole();
        reactionRole.setId(event.getGuild().getId() + ":" + name);

        reactionRole.setName(name);
        reactionRole.setRoleId(Long.parseLong(roleId));
        reactionRole.setGuildId(event.getGuild().getIdLong());
        reactionRole.setMessageId(event.getMessageIdLong());
        reactionRole.setChannelId(event.getChannel().getIdLong());

        reactionRole.setReaction(event.getReactionEmote().getAsReactionCode());

        service.create(reactionRole, member, responseChannelId);
    }

}
