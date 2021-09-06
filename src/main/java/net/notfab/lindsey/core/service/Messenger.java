package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.lindseybot.entities.discord.FMessage;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.response.ButtonResponse;
import net.notfab.lindsey.core.framework.DiscordAdapter;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

@Service
public class Messenger {

    private final Translator i18n;
    private final DiscordAdapter adapter;

    public Messenger(Translator i18n, DiscordAdapter adapter) {
        this.i18n = i18n;
        this.adapter = adapter;
    }

    private String getContent(Label label, Member member, User user) {
        if (label.isLiteral()) {
            return label.getName();
        }
        String content;
        if (member == null) {
            content = this.i18n.get(user, label.getName(), label.getArguments());
        } else {
            content = this.i18n.get(member, label.getName(), label.getArguments());
        }
        return content;
    }

    private Message getContent(FMessage message, Member member) {
        return this.adapter.getMessage(message, member);
    }

    public void reply(ServerCommandEvent event, Label label, boolean ephemeral) {
        String content = getContent(label, event.getMember(), null);
        if (event.getUnderlying().isAcknowledged()) {
            event.getUnderlying().getHook()
                .editOriginal(content)
                .queue();
        } else {
            event.getUnderlying().reply(content)
                .setEphemeral(ephemeral)
                .queue();
        }
    }

    public void reply(SlashCommandEvent event, FMessage message) {
        Message content = getContent(message, event.getMember());
        if (event.isAcknowledged()) {
            event.getHook()
                .sendMessage(content)
                .setEphemeral(message.isEphemeral())
                .queue();
        } else {
            event.reply(content)
                .setEphemeral(message.isEphemeral())
                .queue();
        }
    }

    public void reply(@NotNull ButtonClickEvent event, ButtonResponse response) {
        Message content = getContent(response.getMessage(), event.getMember());
        if (event.isAcknowledged()) {
            event.getHook()
                .sendMessage(content)
                .setEphemeral(response.getMessage().isEphemeral())
                .queue();
        } else {
            event.reply(content)
                .setEphemeral(response.getMessage().isEphemeral())
                .queue();
        }
    }

    public void edit(@NotNull ButtonClickEvent event, ButtonResponse response) {
        Message content = getContent(response.getMessage(), event.getMember());
        if (event.isAcknowledged()) {
            event.getHook()
                .editOriginal(content)
                .queue();
        } else {
            event.editMessage(content)
                .queue();
        }
    }

    public void send(TextChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    public void send(TextChannel channel, MessageEmbed embed) {
        channel.sendMessageEmbeds(embed).queue();
    }

    public void send(TextChannel channel, EmbedBuilder builder) {
        this.send(channel, builder.build());
    }

    public void sendImage(TextChannel channel, String url) {
        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setImage(url);
            this.send(channel, builder.build());
        } else {
            this.send(channel, url);
        }
    }

}
