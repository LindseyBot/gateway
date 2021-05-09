package net.notfab.lindsey.core.framework;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.lindseybot.discord.Embed;
import net.lindseybot.enums.MentionType;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DiscordAdapter {

    private final Translator i18n;
    private final List<Message.MentionType> DEFAULT_ALLOWED_MENTIONS;

    public DiscordAdapter(Translator i18n) {
        this.i18n = i18n;
        this.DEFAULT_ALLOWED_MENTIONS = new ArrayList<>();
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.USER);
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.CHANNEL);
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.EMOTE);
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.ROLE);
    }

    private String parse(net.lindseybot.discord.Message msg, Member member) {
        if (msg.isRaw()) {
            return msg.getName();
        } else {
            return this.i18n.get(member, msg.getName(), msg.getArgs());
        }
    }

    private String parse(net.lindseybot.discord.Message msg, Guild guild) {
        if (msg.isRaw()) {
            return msg.getName();
        } else {
            return this.i18n.get(guild, msg.getName(), msg.getArgs());
        }
    }

    public Message toMessage(net.lindseybot.discord.Message msg, Guild guild) {
        MessageBuilder builder = new MessageBuilder();
        if (msg.getName() != null) {
            builder.setContent(this.parse(msg, guild));
        }
        if (msg.getAllowedMentions().isEmpty()) {
            builder.setAllowedMentions(DEFAULT_ALLOWED_MENTIONS);
        } else {
            List<Message.MentionType> types = new ArrayList<>();
            for (MentionType type : msg.getAllowedMentions()) {
                try {
                    types.add(Message.MentionType.valueOf(type.name()));
                } catch (IllegalArgumentException ex) {
                    log.error("Invalid mention type " + type.name());
                }
            }
            builder.setAllowedMentions(types);
        }
        if (msg.getEmbed() != null) {
            builder.setEmbed(this.buildEmbed(msg.getEmbed(), guild));
        }
        return builder.build();
    }

    public Message toMessage(net.lindseybot.discord.Message msg, Member member) {
        MessageBuilder builder = new MessageBuilder();
        if (msg.getName() != null) {
            builder.setContent(this.parse(msg, member));
        }
        if (msg.getAllowedMentions().isEmpty()) {
            builder.setAllowedMentions(DEFAULT_ALLOWED_MENTIONS);
        } else {
            List<Message.MentionType> types = new ArrayList<>();
            for (MentionType type : msg.getAllowedMentions()) {
                try {
                    types.add(Message.MentionType.valueOf(type.name()));
                } catch (IllegalArgumentException ex) {
                    log.error("Invalid mention type " + type.name());
                }
            }
            builder.setAllowedMentions(types);
        }
        if (msg.getEmbed() != null) {
            builder.setEmbed(this.buildEmbed(msg.getEmbed(), member));
        }
        return builder.build();
    }

    public MessageEmbed buildEmbed(Embed request, Guild guild) {
        EmbedBuilder builder = new EmbedBuilder();
        if (request.getTitle() != null) {
            String text = this.parse(request.getTitle(), guild);
            builder.setTitle(text, request.getUrl());
        }
        if (request.getColor() != null) {
            builder.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            String text = this.parse(request.getDescription(), guild);
            builder.setDescription(text);
        }
        if (request.getImage() != null) {
            builder.setImage(request.getImage());
        }
        if (request.getThumbnail() != null) {
            builder.setThumbnail(request.getThumbnail());
        }
        if (request.getTimestamp() != null) {
            OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneOffset.UTC);
            builder.setTimestamp(time);
        }
        if (request.getAuthor() != null) {
            String name = this.parse(request.getAuthor().getName(), guild);
            builder.setAuthor(name, request.getAuthor().getUrl(), request.getAuthor().getIcon());
        }
        if (request.getFooter() != null) {
            String text = this.parse(request.getFooter().getText(), guild);
            builder.setFooter(text, request.getFooter().getIcon());
        }
        if (!request.getFields().isEmpty()) {
            for (Embed.Field field : request.getFields()) {
                String name = this.parse(field.getName(), guild);
                String value = this.parse(field.getValue(), guild);
                builder.addField(name, value, field.isInline());
            }
        }
        return builder.build();
    }

    public MessageEmbed buildEmbed(Embed request, Member member) {
        EmbedBuilder builder = new EmbedBuilder();
        if (request.getTitle() != null) {
            String text = this.parse(request.getTitle(), member);
            builder.setTitle(text, request.getUrl());
        }
        if (request.getColor() != null) {
            builder.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            String text = this.parse(request.getDescription(), member);
            builder.setDescription(text);
        }
        if (request.getImage() != null) {
            builder.setImage(request.getImage());
        }
        if (request.getThumbnail() != null) {
            builder.setThumbnail(request.getThumbnail());
        }
        if (request.getTimestamp() != null) {
            OffsetDateTime time = OffsetDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneOffset.UTC);
            builder.setTimestamp(time);
        }
        if (request.getAuthor() != null) {
            String name = this.parse(request.getAuthor().getName(), member);
            builder.setAuthor(name, request.getAuthor().getUrl(), request.getAuthor().getIcon());
        }
        if (request.getFooter() != null) {
            String text = this.parse(request.getFooter().getText(), member);
            builder.setFooter(text, request.getFooter().getIcon());
        }
        if (!request.getFields().isEmpty()) {
            for (Embed.Field field : request.getFields()) {
                String name = this.parse(field.getName(), member);
                String value = this.parse(field.getValue(), member);
                builder.addField(name, value, field.isInline());
            }
        }
        return builder.build();
    }

}
