package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.core.Lindsey;
import net.notfab.lindsey.core.framework.GFXUtils;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.service.StarboardService;
import net.notfab.lindsey.shared.entities.Starboard;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Collections;

@Component
public class StarboardListener extends ListenerAdapter {

    private final StarboardService service;

    public StarboardListener(Lindsey lindsey, StarboardService service) {
        lindsey.addEventListener(this);
        this.service = service;
    }

    @Override
    public void onGuildMessageReactionAdd(@NotNull GuildMessageReactionAddEvent event) {
        if (!CommandListener.isAllowed(event.getGuild())) {
            return;
        }
        this.handleStarboard(event.getReactionEmote(), event.getChannel(), event.getMessageIdLong());
    }

    @Override
    public void onGuildMessageReactionRemove(@NotNull GuildMessageReactionRemoveEvent event) {
        if (!CommandListener.isAllowed(event.getGuild())) {
            return;
        }
        this.handleStarboard(event.getReactionEmote(), event.getChannel(), event.getMessageIdLong());
    }

    private void handleStarboard(MessageReaction.ReactionEmote emote, TextChannel channel, long messageId) {
        if (!emote.getName().equals("\u2B50")) {
            return;
        }

        TextChannel starboardChannel = service.getChannel(channel.getGuild());
        if (starboardChannel == null) {
            return;
        }

        Message message = channel.retrieveMessageById(messageId).complete();
        if (message == null)
            return;

        if (((TextChannel) message.getChannel()).isNSFW() && !starboardChannel.isNSFW())
            return;

        MessageReaction reaction = message.getReactions().stream()
            .filter(r -> r.getReactionEmote().getName().equals("\u2B50")).findAny().orElse(null);

        int count = reaction == null ? 0 : reaction.getCount();

        Starboard starboard = service.getStarboard(message, starboardChannel);
        if (count == 0) {
            if (starboard.getStarboardMessageId() != null) {
                starboardChannel.deleteMessageById(starboard.getStarboardMessageId())
                    .queue(Utils.noop(), Utils.noop());
            }
            service.delete(starboard);
            return;
        }
        starboard.setStars(count);
        if (starboard.getStarboardMessageId() != null) {
            starboardChannel.editMessageById(starboard.getStarboardMessageId(), this.createEmbed(starboard, message))
                .queue(Utils.noop(), Utils.noop());
            service.save(starboard);
        } else {
            starboardChannel.sendMessage(this.createEmbed(starboard, message)).queue(msg -> {
                starboard.setStarboardMessageId(msg.getIdLong());
                service.save(starboard);
            }, Utils.noop());
        }
    }

    private Message createEmbed(Starboard starboard, Message message) {
        if (message == null) return null;
        MessageBuilder messageBuilder = new MessageBuilder();
        EmbedBuilder builder = new EmbedBuilder();
        builder.setAuthor(message.getAuthor().getName(),
            message.getJumpUrl(), message.getAuthor().getEffectiveAvatarUrl());

        if (!message.getContentDisplay().isEmpty()) {
            builder.setDescription(message.getContentDisplay()
                .replace("@everyone", "(at)everyone").replace("@here", "(at)here"));
        }
        if (!message.getAttachments().isEmpty()) {
            builder.setImage(message.getAttachments().get(0).getUrl());
        }
        builder.addField("Original", "[Jump](" + message.getJumpUrl() + ")", false);
        builder.setColor(this.getColor(starboard.getStars()));
        builder.setTimestamp(message.getTimeCreated());
        messageBuilder.append(getIcon(starboard.getStars()))
            .append(" ").append(String.valueOf(starboard.getStars()))
            .append(" ").append(message.getTextChannel().getAsMention())
            .append(" ID: ").append(message.getId());
        messageBuilder.setEmbed(builder.build());
        messageBuilder.setAllowedMentions(Collections.singletonList(Message.MentionType.CHANNEL));
        return messageBuilder.build();
    }

    private String getIcon(int stars) {
        if (stars < 5) {
            return "\u2B50"; // Star
        } else if (stars < 10) {
            return "\uD83C\uDF1F"; // Glowing
        } else if (stars < 15) {
            return "\uD83D\uDCAB"; // Dizzy
        } else if (stars < 50) {
            return "\u2728"; // Sparkles
        } else {
            return "\uD83C\uDF20"; // Shooting Star
        }
    }

    private Color getColor(int stars) {
        if (stars < 5) { // 5
            return GFXUtils.getColor("#FFEF99"); // Star
        } else if (stars < 10) { // 15
            return GFXUtils.getColor("#FFE34C"); // Glowing
        } else if (stars < 15) { //
            return GFXUtils.getColor("#FFD700"); // Dizzy
        } else if (stars < 50) {
            return GFXUtils.getColor("#D700FF"); // Sparkles
        } else {
            return GFXUtils.getColor("#0028FF"); // Shooting Star
        }
    }

}
