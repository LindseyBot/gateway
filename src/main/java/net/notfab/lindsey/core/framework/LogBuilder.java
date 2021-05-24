package net.notfab.lindsey.core.framework;

import net.dv8tion.jda.api.entities.*;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.service.AuditService;
import org.graylog2.gelfclient.GelfMessageBuilder;
import org.graylog2.gelfclient.GelfMessageLevel;

public class LogBuilder {

    private final Translator translator;
    private final AuditService service;
    private final GelfMessageBuilder builder;

    public LogBuilder(Translator translator, AuditService service) {
        this.translator = translator;
        this.service = service;
        this.builder = new GelfMessageBuilder("", "discord-gateway");
        this.builder.level(GelfMessageLevel.INFO);
    }

    public LogBuilder message(Guild guild, String i18n, Object... args) {
        this.builder
            .message(this.translator.get(guild, i18n, args))
            .additionalField("guild", guild.getId());
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
            .additionalField("message_id", message.getId())
            .additionalField("message_author_id", message.getAuthor().getId())
            .additionalField("message_author", message.getAuthor().getAsTag())
            .additionalField("message_channel_id", message.getChannel().getId())
            .additionalField("message_channel_name", message.getChannel().getName());
        return this;
    }

    /**
     * Fills in all information from a message.
     *
     * @param message The message.
     */
    public LogBuilder from(Message message) {
        return this.user(message.getAuthor())
            .channel(message.getChannel())
            .cause(message);
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
