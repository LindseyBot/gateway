package net.notfab.lindsey.core.framework.audit;

import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.notfab.lindsey.core.service.AuditService;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;

public class LogBuilder {

    private final AuditService service;
    private final GelfMessageBuilder builder;

    public LogBuilder(AuditService service) {
        this.service = service;
        this.builder = new GelfMessageBuilder("", "discord-gateway");
        this.builder.level(GelfMessageLevel.INFO);
    }

    public LogBuilder guild(Guild guild) {
        this.builder.additionalField("guild", guild.getId());
        return this;
    }

    public LogBuilder message(String message) {
        this.builder.message(message);
        return this;
    }

    public LogBuilder user(Member member) {
        return this.user(member.getUser());
    }

    public LogBuilder user(User user) {
        this.builder
            .additionalField("user_id", user.getId())
            .additionalField("user", user.getAsTag());
        return this;
    }

    public LogBuilder target(Member member) {
        return this.target(member.getUser());
    }

    public LogBuilder target(User user) {
        this.builder
            .additionalField("target_id", user.getId())
            .additionalField("target", user.getAsTag());
        return this;
    }

    public LogBuilder channel(MessageChannel channel) {
        this.builder
            .additionalField("channel_id", channel.getId())
            .additionalField("channel_name", channel.getName())
            .additionalField("channel_type", channel.getType().name());
        return this;
    }

    /**
     * Registers a specific message as the cause of an action.
     *
     * @param message The message.
     */
    public LogBuilder cause(Message message) {
        this.builder
            .additionalField("trigger", "message")
            .additionalField("message", message.getId());
        return this;
    }

    /**
     * Registers a command as the cause of an action.
     *
     * @param event The command.
     */
    public LogBuilder cause(SlashCommandEvent event) {
        this.builder
            .additionalField("trigger", "slash_command")
            .additionalField("command", event.getCommandPath());
        return this;
    }

    /**
     * Fills in all information from a message.
     *
     * @param message The message.
     */
    public LogBuilder from(Message message) {
        LogBuilder builder = this.user(message.getAuthor())
            .channel(message.getChannel())
            .cause(message);
        if (message.isFromGuild()) {
            return builder.guild(message.getGuild());
        } else {
            return builder;
        }
    }

    /**
     * Fills in all information from a command.
     *
     * @param event The command.
     */
    public LogBuilder from(SlashCommandEvent event) {
        LogBuilder builder = this.user(event.getUser())
            .channel(event.getMessageChannel())
            .cause(event);
        if (event.getGuild() != null) { // IntelliJ complains about isFromGuild
            return builder.guild(event.getGuild());
        } else {
            return builder;
        }
    }

    public LogBuilder field(String name, Object value) {
        this.builder.additionalField(name, value);
        return this;
    }

    public LogBuilder warn() {
        this.builder
            .level(GelfMessageLevel.WARNING);
        return this;
    }

    public LogBuilder error() {
        this.builder
            .level(GelfMessageLevel.ERROR);
        return this;
    }

    public void send() {
        this.service.send(this.builder.build());
    }

}
