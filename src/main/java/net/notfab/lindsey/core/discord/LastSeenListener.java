package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.profile.MemberProfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LastSeenListener extends ListenerAdapter implements ExpirationListener<String, Long> {

    private final ExpiringMap<String, Long> memberCache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(1, TimeUnit.MINUTES)
        .expirationListener(this)
        .build();
    private final MongoTemplate template;

    public LastSeenListener(Lindsey lindsey, MongoTemplate template) {
        lindsey.addEventListener(this);
        this.template = template;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage()) {
            return;
        }
        memberCache.put(event.getGuild().getId() + ":" + event.getAuthor().getId(), System.currentTimeMillis());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        memberCache.put(event.getGuild().getId() + ":" + event.getUser().getId(), System.currentTimeMillis());
    }

    @Override
    public void expired(String key, Long value) {
        Query query = new Query(Criteria.where("_id").is(key));
        Update update = Update.update("lastSeen", null)
            .set("lastSeen", value);
        this.template.findAndModify(query, update, MemberProfile.class);
    }

}
