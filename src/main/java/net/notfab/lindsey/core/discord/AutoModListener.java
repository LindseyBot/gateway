package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.service.AutoModService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class AutoModListener extends ListenerAdapter {

    private final AutoModService service;

    public AutoModListener(AutoModService service) {
        this.service = service;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (this.isExempt(event.getMessage(), event.getMember(), event.getChannel())) {
            return;
        }
        if (!this.service.isEnabled(event.getGuild().getIdLong())) {
            return;
        }
        this.service.moderate(event.getMessage(), event.getMember());
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (this.isExempt(event.getMessage(), event.getMember(), event.getChannel())) {
            return;
        }
        if (!this.service.isEnabled(event.getGuild().getIdLong())) {
            return;
        }
        this.service.moderate(event.getMessage(), event.getMember());
    }

    private boolean isExempt(Message message, Member member, TextChannel channel) {
        if (message == null || member == null || channel == null) {
            return true;
        }
        if (message.isWebhookMessage()) {
            return true;
        }
        if (member.getUser().isBot()) {
            return true;
        }
        if (Utils.isDiscordModerator(member)) {
            return true;
        }
        return channel.isNews();
    }

}
