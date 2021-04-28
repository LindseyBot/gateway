package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jodah.expiringmap.ExpirationListener;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class UserHistoryListener extends ListenerAdapter implements ExpirationListener<Long, String> {

    private final ExpiringMap<Long, String> cache = ExpiringMap.builder()
        .expirationPolicy(ExpirationPolicy.ACCESSED)
        .expiration(1, TimeUnit.MINUTES)
        .expirationListener(this)
        .build();
    private final ProfileManager profiles;

    public UserHistoryListener(Lindsey lindsey, ProfileManager profiles) {
        lindsey.addEventListener(this);
        this.profiles = profiles;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (!CommandListener.isAllowed(event.getGuild())) {
            return;
        }
        String name = event.getAuthor().getAsTag();
        cache.put(event.getAuthor().getIdLong(), name);
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        String name = event.getEntity().getUser().getAsTag();
        cache.put(event.getEntity().getUser().getIdLong(), name);
    }

    @Override
    public void expired(Long id, String name) {
        UserProfile profile = profiles.getUser(id);
        profile.setName(name);
        profile.setLastSeen(System.currentTimeMillis());
        profiles.save(profile);
    }

}
