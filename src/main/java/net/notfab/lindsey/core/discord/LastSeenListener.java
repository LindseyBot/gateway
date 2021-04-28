package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.shared.repositories.sql.MemberProfileRepository;
import net.notfab.lindsey.shared.repositories.sql.ServerProfileRepository;
import net.notfab.lindsey.shared.repositories.sql.UserProfileRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LastSeenListener extends ListenerAdapter implements ExpirationListener<String, Long> {

    private final ExpiringMap<String, Long> memberCache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(1, TimeUnit.MINUTES)
        .expirationListener(this)
        .build();

    private final ExpiringMap<Long, Long> guildCache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(5, TimeUnit.MINUTES)
        .expirationListener(this::guildExpired)
        .build();

    private final MemberProfileRepository memberRepository;
    private final ServerProfileRepository serverRepository;
    private final UserProfileRepository userRepository;

    public LastSeenListener(Lindsey lindsey, MemberProfileRepository memberRepository,
                            ServerProfileRepository serverRepository, UserProfileRepository userRepository) {
        this.memberRepository = memberRepository;
        this.serverRepository = serverRepository;
        this.userRepository = userRepository;
        lindsey.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.isWebhookMessage()) {
            return;
        }
        if (!CommandListener.isAllowed(event.getGuild())) {
            return;
        }
        memberCache.put(event.getGuild().getId() + ":" + event.getAuthor().getId(), System.currentTimeMillis());
        guildCache.put(event.getGuild().getIdLong(), System.currentTimeMillis());
    }

    @Override
    public void onGuildMemberRemove(@NotNull GuildMemberRemoveEvent event) {
        if (!CommandListener.isAllowed(event.getGuild())) {
            return;
        }
        memberCache.put(event.getGuild().getId() + ":" + event.getUser().getId(), System.currentTimeMillis());
        guildCache.put(event.getGuild().getIdLong(), System.currentTimeMillis());
    }

    @Override
    public void expired(String key, Long time) {
        long guild = Long.parseLong(key.split(":")[0]);
        long user = Long.parseLong(key.split(":")[1]);
        this.memberRepository.updateLastSeen(time, user, guild);
        this.userRepository.updateLastSeen(time, user);
    }

    private void guildExpired(Object k, Object v) {
        long guild = (Long) k;
        long time = (Long) v;
        this.serverRepository.updateLastSeen(time, guild);
    }

}
