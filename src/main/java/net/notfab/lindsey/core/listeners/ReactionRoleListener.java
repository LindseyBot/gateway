package net.notfab.lindsey.core.listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.framework.events.MessageReactionAddedEvent;
import net.notfab.lindsey.core.framework.events.MessageReactionRemovedEvent;
import net.notfab.lindsey.core.service.EventService;
import net.notfab.lindsey.core.service.ReactionRoleService;
import net.notfab.lindsey.shared.entities.ReactionRole;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReactionRoleListener implements Listener {

    private final StringRedisTemplate redis;
    private final ReactionRoleService service;

    public ReactionRoleListener(EventService events, StringRedisTemplate redis, ReactionRoleService service) {
        this.redis = redis;
        this.service = service;
        events.addListener(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMessageReactionAddedEvent(@NotNull MessageReactionAddedEvent event) {
        if (this.createReactionRole(event)) {
            event.setCancelled(true);
            return;
        }
        List<ReactionRole> roleList = service.findAll(event.getGuild());
        if (roleList.isEmpty()) {
            return;
        }
        roleList.stream()
            .filter(reactionRole -> event.getMessageId() == reactionRole.getMessageId())
            .filter(reactionRole -> reactionRole.getReaction().equals(event.getReaction().getAsReactionCode()))
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

    @EventHandler(ignoreCancelled = true)
    public void onMessageReactionRemovedEvent(@NotNull MessageReactionRemovedEvent event) {
        List<ReactionRole> roleList = service.findAll(event.getGuild());
        if (roleList.isEmpty()) {
            return;
        }
        roleList.stream()
            .filter(reactionRole -> event.getMessageId() == reactionRole.getMessageId())
            .filter(reactionRole -> reactionRole.getReaction().equals(event.getReaction().getAsReactionCode()))
            .forEach(reactionRole -> {
                Guild guild = event.getGuild();
                Role role = guild.getRoleById(reactionRole.getRoleId());
                if (role == null) {
                    service.remove(guild, reactionRole.getName());
                    return;
                }
                guild.removeRoleFromMember(event.getMember().getIdLong(), role)
                    .reason("Reaction Roles (" + reactionRole.getName() + ")")
                    .queue();
            });
    }

    public boolean createReactionRole(MessageReactionAddedEvent event) {
        String id = "ReactionRole:Creation:" + event.getGuild().getId();
        Boolean hasKey = redis.hasKey(id);
        if (hasKey == null || !hasKey) {
            return false;
        }
        String member = (String) redis.opsForHash().get(id, "member");
        if (!event.getMember().getId().equals(member)) {
            return false;
        }
        String roleId = (String) redis.opsForHash().get(id, "role");
        if (roleId == null) {
            return false;
        }
        String responseChannelId = (String) redis.opsForHash().get(id, "channel");
        if (responseChannelId == null) {
            return false;
        }
        String name = (String) redis.opsForHash().get(id, "name");
        if (name == null) {
            return false;
        }
        ReactionRole reactionRole = new ReactionRole();
        reactionRole.setId(event.getGuild().getId() + ":" + name);

        reactionRole.setName(name);
        reactionRole.setRoleId(Long.parseLong(roleId));
        reactionRole.setGuildId(event.getGuild().getIdLong());
        reactionRole.setMessageId(event.getMessageId());
        reactionRole.setChannelId(event.getChannel().getIdLong());

        reactionRole.setReaction(event.getReaction().getAsReactionCode());

        service.create(reactionRole, member, responseChannelId);
        return true;
    }

}
