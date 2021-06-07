package net.notfab.lindsey.core.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.jodah.expiringmap.ExpirationListener;
import net.lindseybot.services.EventService;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.eventti.ListenerPriority;
import net.notfab.lindsey.core.framework.embeds.WebsiteEmbedder;
import net.notfab.lindsey.core.framework.events.ServerMessageReceivedEvent;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import net.notfab.lindsey.shared.entities.profile.server.BetterEmbedsSettings;
import net.notfab.lindsey.shared.repositories.sql.BetterEmbedSettingsRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class BetterEmbedsListener implements Listener, ExpirationListener<Long, String> {

    private final ProfileManager profiles;
    private final List<WebsiteEmbedder> embedders;
    private final BetterEmbedSettingsRepository repository;

    public BetterEmbedsListener(EventService events, ProfileManager profiles, List<WebsiteEmbedder> embedders,
                                BetterEmbedSettingsRepository repository) {
        this.profiles = profiles;
        this.embedders = embedders;
        this.repository = repository;
        events.addListener(this);
    }

    /**
     * Fired when a message is received, basic checks have already been done.
     *
     * @param event Cancellable event.
     */
    @EventHandler(priority = ListenerPriority.HIGH, ignoreCancelled = true)
    public void onGuildMessageReceived(@NotNull ServerMessageReceivedEvent event) {
        Member member = event.getMember();
        TextChannel channel = event.getChannel();
        MessageEmbed embed;
        for (WebsiteEmbedder builder : this.embedders) {
            if (!builder.isSupported(event.getMessage().getContentRaw())) {
                continue;
            }
            BetterEmbedsSettings settings = this.repository.findById(member.getGuild().getIdLong())
                .orElse(new BetterEmbedsSettings(member.getGuild().getIdLong()));
            if (!builder.isEnabled(settings)) {
                return;
            }
            try {
                embed = builder.getEmbed(event.getMessage().getContentRaw(), member, channel.isNSFW());
            } catch (Exception ex) {
                log.error("Failed to generate better embed", ex);
                return;
            }
            if (embed == null) {
                return;
            }
            event.setCancelled(true);
            event.getMessage()
                .delete()
                .flatMap((d) -> event.getChannel().sendMessage(embed))
                .queue();
            break;
        }
    }

    @Override
    public void expired(Long id, String name) {
        UserProfile profile = profiles.getUser(id);
        profile.setName(name);
        profile.setLastSeen(System.currentTimeMillis());
        profiles.save(profile);
    }

}
