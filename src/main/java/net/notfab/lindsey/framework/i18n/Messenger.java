package net.notfab.lindsey.framework.i18n;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.springframework.stereotype.Service;

@Service
public class Messenger {

    public void send(TextChannel channel, String message) {
        channel.sendMessage(message).queue();
    }

    public void send(TextChannel channel, MessageEmbed embed) {
        channel.sendMessage(embed).queue();
    }

    public void send(TextChannel channel, EmbedBuilder builder) {
        this.send(channel, builder.build());
    }

    public void sendImage(TextChannel channel, String url) {
        if (channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_EMBED_LINKS)) {
            EmbedBuilder builder = new EmbedBuilder();
            builder.setImage(url);
            this.send(channel, builder.build());
        }
        else{
            send(channel, url);
        }
    }
}
