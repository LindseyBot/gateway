package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.PlaceHolderUtils;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.shared.entities.server.WelcomeSettings;
import net.notfab.lindsey.shared.repositories.sql.WelcomeSettingsRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WelcomeListener extends ListenerAdapter {

    private final WelcomeSettingsRepository repository;

    public WelcomeListener(Lindsey lindsey, WelcomeSettingsRepository repository) {
        lindsey.addEventListener(this);
        this.repository = repository;
    }

    @Override
    public void onGuildMemberJoin(@NotNull GuildMemberJoinEvent event) {
        if (!CommandListener.isAllowed(event.getGuild())) {
            return;
        }
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
