package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.profile.guild.ReactionRole;
import net.notfab.lindsey.core.repositories.mongo.ReactionRoleRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class ReactionRoleService {

    private final ReactionRoleRepository repository;
    private final StringRedisTemplate redis;
    private final ShardManager shardManager;

    private final Translator i18n;
    private final Messenger msg;

    private final ExpiringMap<Long, List<ReactionRole>> cache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(5, TimeUnit.MINUTES)
        .build();

    public ReactionRoleService(ReactionRoleRepository repository, StringRedisTemplate redis,
                               ShardManager shardManager, Translator i18n, Messenger msg) {
        this.repository = repository;
        this.redis = redis;
        this.shardManager = shardManager;
        this.i18n = i18n;
        this.msg = msg;
    }

    /**
     * Starts the creation process for a Reaction Role.
     *
     * @param role    - Target role.
     * @param member  - Admin who triggered the action.
     * @param channel - Channel where the command was triggered.
     * @return If the process was started (false in case one already exists).
     */
    public boolean create(Role role, String name, Member member, TextChannel channel) {
        String id = "ReactionRole:Creation:" + role.getGuild().getId();
        Boolean hasKey = redis.hasKey(id);
        if (hasKey != null && hasKey) {
            return false;
        }
        redis.opsForHash().put(id, "member", member.getId());
        redis.opsForHash().put(id, "role", role.getId());
        redis.opsForHash().put(id, "channel", channel.getId());
        redis.opsForHash().put(id, "name", name);
        redis.expire(id, 1, TimeUnit.MINUTES);
        return true;
    }

    /**
     * Creates a reaction role, saving in the database.
     *
     * @param reactionRole - Populated ReactionRole.
     * @param memberId     - Admin who called the command.
     * @param channelId    - Channel where the command was called.
     */
    public void create(ReactionRole reactionRole, String memberId, String channelId) {
        repository.save(reactionRole);
        redis.delete("ReactionRole:Creation:" + reactionRole.getGuildId());

        Guild guild = shardManager.getGuildById(reactionRole.getGuildId());
        if (guild == null) {
            return;
        }
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            return;
        }
        Member admin = guild.getMemberById(memberId);
        if (admin == null) {
            return;
        }
        Role role = guild.getRoleById(reactionRole.getRoleId());
        if (role == null) {
            return;
        }
        String reaction = reactionRole.getReaction();
        if (reaction.contains(":")) {
            reaction = "<:" + reaction + ">";
        }
        msg.send(channel, "**" + admin.getEffectiveName() + "**: " + i18n.get(admin, "commands.mod.reactionroles.created",
            reactionRole.getName(),
            i18n.get(admin, "embeds.reactionroles.line", reaction, channel.getAsMention(), role.getName())));
        cache.remove(guild.getIdLong());
    }

    /**
     * Permanently removes a reaction role.
     *
     * @param guild - Guild.
     * @param name  - Name.
     * @return If was removed.
     */
    public boolean remove(Guild guild, String name) {
        Optional<ReactionRole> oReaction = repository.findById(guild.getId() + ":" + name);
        if (oReaction.isEmpty()) {
            return false;
        }
        repository.delete(oReaction.get());
        cache.remove(guild.getIdLong());
        return true;
    }

    public List<ReactionRole> findAll(Guild guild) {
        if (cache.containsKey(guild.getIdLong())) {
            return cache.get(guild.getIdLong());
        } else {
            List<ReactionRole> reactionRoles = repository.findAllByGuildId(guild.getIdLong());
            cache.put(guild.getIdLong(), reactionRoles);
            return reactionRoles;
        }
    }

}
