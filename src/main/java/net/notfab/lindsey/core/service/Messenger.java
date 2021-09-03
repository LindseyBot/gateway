package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.lindseybot.entities.discord.Label;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import org.springframework.stereotype.Service;

@Service
public class Messenger {

    private final Translator i18n;

    public Messenger(Translator i18n) {
        this.i18n = i18n;
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

}
