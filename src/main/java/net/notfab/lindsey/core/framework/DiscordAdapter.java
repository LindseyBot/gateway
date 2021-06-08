package net.notfab.lindsey.core.framework;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.Component;
import net.lindseybot.discord.Button;
import net.lindseybot.discord.Embed;
import net.lindseybot.discord.MessageComponent;
import net.lindseybot.enums.MentionType;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Value;
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
    private final String ENCRYPTION_TOKEN;
    private final List<Message.MentionType> DEFAULT_ALLOWED_MENTIONS;

    public DiscordAdapter(Translator i18n, @Value("bot.encryption") String token) {
        this.i18n = i18n;
        this.ENCRYPTION_TOKEN = token;
        this.DEFAULT_ALLOWED_MENTIONS = new ArrayList<>();
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.USER);
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.CHANNEL);
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.EMOTE);
        this.DEFAULT_ALLOWED_MENTIONS.add(Message.MentionType.ROLE);
    }

    private String getLabel(net.lindseybot.discord.Message msg, ISnowflake snowflake) {
        if (msg.isRaw()) {
            return msg.getName();
        } else if (snowflake instanceof Member) {
            Member member = (Member) snowflake;
            return this.i18n.get(member, msg.getName(), msg.getArgs());
        } else if (snowflake instanceof Guild) {
            Guild guild = (Guild) snowflake;
            return this.i18n.get(guild, msg.getName(), msg.getArgs());
        } else {
            throw new IllegalArgumentException("Unknown snowflake holder");
        }
    }

    public Message toMessage(net.lindseybot.discord.Message msg, ISnowflake snowflake) {
        MessageBuilder builder = new MessageBuilder();
        if (msg.getName() != null) {
            builder.setContent(this.getLabel(msg, snowflake));
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
            builder.setEmbed(this.buildEmbed(msg.getEmbed(), snowflake));
        }
        if (!msg.getComponents().isEmpty()) {
            List<ActionRow> rows = new ArrayList<>();
            List<Component> components = new ArrayList<>();
            int i = 0;
            for (MessageComponent component : msg.getComponents()) {
                if (component instanceof Button) {
                    Button model = (Button) component;
                    components.add(this.createButton(model, snowflake));
                }
                i++;
                if (i % 5 == 0) {
                    ActionRow row = ActionRow.of(components);
                    rows.add(row);
                    components.clear();
                    i = 0;
                }
            }
            if (!components.isEmpty()) {
                ActionRow row = ActionRow.of(components);
                rows.add(row);
            }
            builder.setActionRows(rows);
        }
        return builder.build();
    }

    private net.dv8tion.jda.api.interactions.components.Button createButton(Button model, ISnowflake snowflake) {
        ButtonStyle style = ButtonStyle.valueOf(model.getStyle().name());
        String id;
        if (style == ButtonStyle.LINK) {
            id = model.getIdOrUrl();
        } else {
            id = EncryptionUtils.aesEcojiEncrypt(ENCRYPTION_TOKEN,
                model.getIdOrUrl() + ":" + model.getUserFilter() + ":" + model.getData());
            if (id == null) {
                throw new IllegalStateException("Failed to encode button id");
            } else if (id.codePointCount(0, id.length()) > 100) {
                throw new IllegalStateException("Button id too big " + model.getIdOrUrl());
            }
        }
        net.dv8tion.jda.api.interactions.components.Button button = net.dv8tion.jda.api.interactions.components.Button
            .of(style, id, this.getLabel(model.getLabel(), snowflake));
        if (model.getEmote() != null) {
            button.withEmoji(this.toEmoji(model.getEmote()));
        }
        if (model.isDisabled()) {
            return button.asDisabled();
        } else {
            return button;
        }
    }

    private Emoji toEmoji(net.lindseybot.discord.Emote emote) {
        if (emote.isUnicode()) {
            return Emoji.ofUnicode(emote.getName());
        } else {
            return Emoji.ofEmote(emote.getName(), emote.getId(), emote.isAnimated());
        }
    }

    public MessageEmbed buildEmbed(Embed request, ISnowflake snowflake) {
        EmbedBuilder builder = new EmbedBuilder();
        if (request.getTitle() != null) {
            String text = this.getLabel(request.getTitle(), snowflake);
            builder.setTitle(text, request.getUrl());
        }
        if (request.getColor() != null) {
            builder.setColor(request.getColor());
        }
        if (request.getDescription() != null) {
            String text = this.getLabel(request.getDescription(), snowflake);
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
            String name = this.getLabel(request.getAuthor().getName(), snowflake);
            builder.setAuthor(name, request.getAuthor().getUrl(), request.getAuthor().getIcon());
        }
        if (request.getFooter() != null) {
            String text = this.getLabel(request.getFooter().getText(), snowflake);
            builder.setFooter(text, request.getFooter().getIcon());
        }
        if (!request.getFields().isEmpty()) {
            for (Embed.Field field : request.getFields()) {
                String name = this.getLabel(field.getName(), snowflake);
                String value = this.getLabel(field.getValue(), snowflake);
                builder.addField(name, value, field.isInline());
            }
        }
        return builder.build();
    }

}
