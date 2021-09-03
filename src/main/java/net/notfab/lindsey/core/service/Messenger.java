package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.lindseybot.entities.discord.Label;
import net.notfab.lindsey.core.framework.Emotes;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.shared.entities.profile.server.MusicSettings;
import net.notfab.lindsey.shared.repositories.sql.server.MusicSettingsRepository;
import org.springframework.stereotype.Service;

@Service
public class Messenger {

    private final Translator i18n;
    private final ShardManager shardManager;
    private final MusicSettingsRepository musicSettings;

    public Messenger(Translator i18n, ShardManager shardManager, MusicSettingsRepository musicSettings) {
        this.i18n = i18n;
        this.shardManager = shardManager;
        this.musicSettings = musicSettings;
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

    public void reply(ButtonClickEvent event, Label label, boolean ephemeral) {
        String content = this.getContent(label, event.getMember(), event.getUser());
        event.reply(content)
            .setEphemeral(ephemeral)
            .queue();
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

    public void sendMusic(long guildId, String message) {
        Guild guild = this.shardManager.getGuildById(guildId);
        if (guild == null) {
            return;
        }
        MusicSettings settings = this.musicSettings.findById(guildId)
            .orElse(new MusicSettings(guildId));
        if (!settings.isLogTracks()) {
            return;
        }
        TextChannel channel = guild.getTextChannelById(settings.getLogChannel());
        if (channel == null) {
            settings.setLogTracks(false);
            settings.setLogChannel(null);
            this.musicSettings.save(settings);
            return;
        }
        this.send(channel, Emotes.MUSIC_LOGO.asEmote() + " " + message);
    }

}
