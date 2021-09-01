package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.command.OptionMapper;
import net.notfab.lindsey.core.framework.events.*;
import net.notfab.lindsey.core.service.EventService;
import net.notfab.lindsey.core.service.IgnoreService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DiscordListener extends ListenerAdapter {

    private final EventService events;
    private final IgnoreService ignores;

    public DiscordListener(Lindsey lindsey, EventService events, IgnoreService ignores) {
        this.events = events;
        this.ignores = ignores;
        lindsey.addEventListener(this);
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.isWebhookMessage() || event.getMember() == null) {
            return;
        }

        if (isNotAllowed(event.getGuild())) {
            return;
        }

        if (this.ignores.isIgnored(event.getGuild().getIdLong(), event.getChannel().getIdLong())) {
            return;
        }

        ServerMessageReceivedEvent localEvent = new ServerMessageReceivedEvent();
        localEvent.setMember(event.getMember());
        localEvent.setGuild(event.getGuild());
        localEvent.setChannel(event.getChannel());
        localEvent.setMessage(event.getMessage());

        /*
         * TODO: Document hierarchy, usually is
         * Commands -> AutoMod -> Others -> Loggers
         */
        this.events.fire(localEvent);
    }

    @Override
    public void onGuildMessageUpdate(@NotNull GuildMessageUpdateEvent event) {
        if (event.getAuthor().isBot() || event.getMessage().isWebhookMessage() || event.getMember() == null) {
            return;
        }

        if (isNotAllowed(event.getGuild())) {
            return;
        }

        if (this.ignores.isIgnored(event.getGuild().getIdLong(), event.getChannel().getIdLong())) {
            return;
        }

        ServerMessageUpdatedEvent localEvent = new ServerMessageUpdatedEvent();
        localEvent.setMember(event.getMember());
        localEvent.setGuild(event.getGuild());
        localEvent.setChannel(event.getChannel());
        localEvent.setMessage(event.getMessage());

        this.events.fire(localEvent);
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (event.getMember().getUser().isBot()) {
            return;
        }

        if (isNotAllowed(event.getGuild())) {
            return;
        }

        if (this.ignores.isIgnored(event.getGuild().getIdLong(), event.getChannel().getIdLong())) {
            return;
        }

        MessageReactionAddedEvent localEvent = new MessageReactionAddedEvent();
        localEvent.setMessageId(event.getMessageIdLong());
        localEvent.setMember(event.getMember());
        localEvent.setGuild(event.getGuild());
        localEvent.setChannel(event.getChannel());
        localEvent.setReaction(event.getReactionEmote());

        this.events.fire(localEvent);
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (event.getMember() == null || event.getMember().getUser().isBot()) {
            return;
        }

        if (isNotAllowed(event.getGuild())) {
            return;
        }

        if (this.ignores.isIgnored(event.getGuild().getIdLong(), event.getChannel().getIdLong())) {
            return;
        }

        MessageReactionRemovedEvent localEvent = new MessageReactionRemovedEvent();
        localEvent.setMessageId(event.getMessageIdLong());
        localEvent.setMember(event.getMember());
        localEvent.setGuild(event.getGuild());
        localEvent.setChannel(event.getChannel());
        localEvent.setReaction(event.getReactionEmote());

        this.events.fire(localEvent);
    }

    @Override
    public void onSlashCommand(@NotNull SlashCommandEvent event) {
        if (event.getMember() == null || event.getMember().getUser().isBot()) {
            return;
        } else if (event.getGuild() == null) {
            return;
        } else if (isNotAllowed(event.getGuild())) {
            event.reply("No authorization.").setEphemeral(true)
                .queue();
            return;
        } else if (event.getMember().isPending()) {
            event.reply("Please complete membership screening before executing any commands.").setEphemeral(true)
                .queue();
            return;
        } else if (this.ignores.isIgnored(event.getGuild().getIdLong(), event.getChannel().getIdLong())) {
            return;
        }

        ServerCommandEvent localEvent = new ServerCommandEvent();
        localEvent.setMember(event.getMember());
        localEvent.setGuild(event.getGuild());
        localEvent.setChannel(event.getTextChannel());
        localEvent.setPath(event.getCommandPath());
        localEvent.setOptions(new OptionMapper(event.getOptions()));
        localEvent.setUnderlying(event);

        this.events.fire(localEvent);
    }

    // TODO: Remove for prod
    private boolean isNotAllowed(Guild guild) {
        Set<Long> ids = new HashSet<>();
        ids.add(141555945586163712L);
        ids.add(213044545825406976L);
        ids.add(382281038547648512L);
        return !ids.contains(guild.getIdLong());
    }

}
