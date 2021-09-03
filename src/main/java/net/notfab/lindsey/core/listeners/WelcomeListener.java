package net.notfab.lindsey.core.listeners;

import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.framework.PlaceHolderUtils;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.events.ServerMemberJoinEvent;
import net.notfab.lindsey.core.repositories.sql.WelcomeSettingsRepository;
import net.notfab.lindsey.core.service.EventService;
import net.notfab.lindsey.shared.entities.server.WelcomeSettings;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WelcomeListener implements Listener {

    private final WelcomeSettingsRepository repository;

    public WelcomeListener(EventService events, WelcomeSettingsRepository repository) {
        this.repository = repository;
        events.addListener(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onServerMemberJoinEvent(@NotNull ServerMemberJoinEvent event) {
        Optional<WelcomeSettings> oSettings = repository.findById(event.getGuild().getIdLong());
        if (oSettings.isEmpty()) {
            return;
        }
        WelcomeSettings settings = oSettings.get();
        if (!settings.isEnabled()) {
            return;
        }
        String msg = settings.getMessage();
        if (msg == null) {
            settings.setEnabled(false);
            repository.save(settings);
            return;
        }
        String finalMsg = PlaceHolderUtils.replace(msg, event.getMember());
        if (settings.isDms()) {
            event.getMember()
                .getUser()
                .openPrivateChannel()
                .flatMap(channel -> channel.sendMessage(finalMsg))
                .queue(Utils.noop(), Utils.noop());
        } else {
            TextChannel channel = event.getGuild().getTextChannelById(settings.getChannelId());
            if (channel == null) {
                settings.setEnabled(false);
                repository.save(settings);
                return;
            }
            channel.sendMessage(finalMsg)
                .queue(Utils.noop(), Utils.noop());
        }
    }

}
