package net.notfab.lindsey.core.framework.i18n;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.framework.Emotes;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import org.springframework.stereotype.Service;

@Service
public class Messenger {

    private final ShardManager shardManager;
    private final ProfileManager profileManager;

    public Messenger(ShardManager shardManager, ProfileManager profileManager) {
        this.shardManager = shardManager;
        this.profileManager = profileManager;
    }

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
        } else {
            this.send(channel, url);
        }
    }

    public void sendMusic(long guildId, String message) {
        Guild guild = this.shardManager.getGuildById(guildId);
        if (guild == null) {
            return;
        }
        ServerProfile profile = this.profileManager.getGuild(guildId);
        Long channelId = profile.getMusicChannelId();
        if (channelId == null) {
            return;
        }
        TextChannel channel = guild.getTextChannelById(channelId);
        if (channel == null) {
            profile.setMusicChannelId(null);
            this.profileManager.save(profile);
            return;
        }
        this.send(channel, Emotes.MUSIC_LOGO.asEmote() + " " + message);
    }

}
