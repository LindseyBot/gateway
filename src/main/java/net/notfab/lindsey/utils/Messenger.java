package net.notfab.lindsey.utils;

import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.PermissionException;

import java.util.function.Consumer;

public class Messenger {

    private static Consumer<Message> successEmpty = (m) -> {
    };
    private static Consumer<Throwable> errorEmpty = (x) -> {
    };

    /**
     * Sends a message to a guild channel.
     *
     * @param channel - Text Channel to send to.
     * @param builder - Message Builder.
     */
    public static void send(TextChannel channel, MessageBuilder builder) {
        if (channel == null || !channel.canTalk()) {
            return;
        }
        builder = builder.stripMentions(channel.getJDA(), Message.MentionType.EVERYONE);
        builder = builder.stripMentions(channel.getJDA(), Message.MentionType.HERE);
        try {
            channel.sendMessage(builder.build()).queue(successEmpty, errorEmpty);
        } catch (PermissionException ignored) {
            // Ignored
        }
    }

    /**
     * Sends a message without filtering mentions.
     *
     * @param channel - Text Channel to send to.
     * @param builder - Message Builder.
     */
    public static void sendOverride(TextChannel channel, MessageBuilder builder) {
        if (channel == null || !channel.canTalk()) {
            return;
        }
        channel.sendMessage(builder.build()).queue(successEmpty, errorEmpty);
    }

    /**
     * Sends a message to a private channel.
     *
     * @param channel - Private Channel to send to.
     * @param builder - Message Builder.
     */
    public static void send(PrivateChannel channel, MessageBuilder builder) {
        if (channel == null || channel.isFake()) {
            return;
        }
        builder = builder.stripMentions(channel.getJDA(), Message.MentionType.EVERYONE);
        builder = builder.stripMentions(channel.getJDA(), Message.MentionType.HERE);
        channel.sendMessage(builder.build()).queue(successEmpty, errorEmpty);
    }

    /**
     * Sends a message without filtering mentions.
     *
     * @param channel - Private Channel to send to.
     * @param builder - Message Builder.
     */
    public static void sendOverride(PrivateChannel channel, MessageBuilder builder) {
        if (channel == null || channel.isFake()) {
            return;
        }
        channel.sendMessage(builder.build()).queue(successEmpty, errorEmpty);
    }

    // -- Overloads [TextChannel]

    public static void send(TextChannel channel, String message) {
        send(channel, new MessageBuilder(message));
    }

    public static void sendOverride(TextChannel channel, String message) {
        sendOverride(channel, new MessageBuilder(message));
    }

    public static void send(TextChannel channel, MessageEmbed embed) {
        send(channel, new MessageBuilder().setEmbed(embed));
    }

    public static void sendOverride(TextChannel channel, MessageEmbed embed) {
        sendOverride(channel, new MessageBuilder().setEmbed(embed));
    }

    // -- Overloads [PrivateChannel]

    public static void send(PrivateChannel channel, String message) {
        send(channel, new MessageBuilder(message));
    }

    public static void sendOverride(PrivateChannel channel, String message) {
        sendOverride(channel, new MessageBuilder(message));
    }

    public static void send(PrivateChannel channel, MessageEmbed embed) {
        send(channel, new MessageBuilder().setEmbed(embed));
    }

    public static void sendOverride(PrivateChannel channel, MessageEmbed embed) {
        sendOverride(channel, new MessageBuilder().setEmbed(embed));
    }

    // -- Overloads [User and Member]

    public static void send(User user, MessageBuilder builder) {
        if (user == null || user.isFake() || user.isBot()) {
            return;
        }
        user.openPrivateChannel().queue(c -> send(c, builder), errorEmpty);
    }

    public static void sendOverride(User user, MessageBuilder builder) {
        if (user == null || user.isFake() || user.isBot()) {
            return;
        }
        user.openPrivateChannel().queue(c -> sendOverride(c, builder), errorEmpty);
    }

    public static void send(User user, String message) {
        if (user == null || user.isFake() || user.isBot()) {
            return;
        }
        user.openPrivateChannel().queue(c -> send(c, message), errorEmpty);
    }

    public static void sendOverride(User user, String message) {
        if (user == null || user.isFake() || user.isBot()) {
            return;
        }
        user.openPrivateChannel().queue(c -> sendOverride(c, message), errorEmpty);
    }

    public static void send(User user, MessageEmbed embed) {
        if (user == null || user.isFake() || user.isBot()) {
            return;
        }
        user.openPrivateChannel().queue(c -> send(c, embed), errorEmpty);
    }

    public static void sendOverride(User user, MessageEmbed embed) {
        if (user == null || user.isFake() || user.isBot()) {
            return;
        }
        user.openPrivateChannel().queue(c -> sendOverride(c, embed), errorEmpty);
    }

    public static void send(Member member, MessageBuilder builder) {
        send(member.getUser(), builder);
    }

    public static void sendOverride(Member member, MessageBuilder builder) {
        sendOverride(member.getUser(), builder);
    }

    public static void send(Member member, String message) {
        send(member.getUser(), message);
    }

    public static void sendOverride(Member member, String message) {
        sendOverride(member.getUser(), message);
    }

    public static void send(Member member, MessageEmbed embed) {
        send(member.getUser(), embed);
    }

    public static void sendOverride(Member member, MessageEmbed embed) {
        sendOverride(member.getUser(), embed);
    }

}
