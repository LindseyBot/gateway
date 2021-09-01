package net.notfab.lindsey.core.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandGroupData;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.events.CommandMetaEvent;
import net.lindseybot.entities.interaction.commands.*;
import net.notfab.lindsey.core.framework.DiscordAdapter;
import net.notfab.lindsey.shared.enums.Language;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class MetaListener {

    private final DiscordAdapter adapter;
    private final ShardManager shardManager;

    public MetaListener(DiscordAdapter adapter, ShardManager shardManager) {
        this.adapter = adapter;
        this.shardManager = shardManager;
    }

    /**
     * Registers and unregisters a slash command on Discord.
     *
     * @param event CommandMeta event.
     */
    @RabbitListener(bindings = {
        @QueueBinding(value = @Queue(), exchange = @Exchange("events"), key = {"events.commands.meta"})
    })
    public void onCommandMeta(@Payload CommandMetaEvent event) {
        CommandMeta model = event.getModel();
        if (!event.isCreate()) {
            log.info("Skipped unregistering a command on Discord ({})", model.getName());
            return;
        }
        CommandData data;
        try {
            data = this.getData(model);
        } catch (Exception ex) {
            log.error("Failed to convert command to Discord format", ex);
            return;
        }
        if (!model.getGuilds().isEmpty()) {
            for (Long guildId : model.getGuilds()) {
                Guild guild = this.shardManager.getGuildById(guildId);
                if (guild == null) {
                    log.warn("Failed to register slash command for guild " + guildId);
                    continue;
                }
                guild.upsertCommand(data).queue();
            }
        } else {
            this.shardManager.getShards().get(0).upsertCommand(data).queue();
        }
    }

    private String toLabel(Label label) {
        return this.adapter.getLabel(label, Language.en_US);
    }

    private CommandData getData(CommandMeta meta) {
        CommandData data = new CommandData(meta.getName(), this.toLabel(meta.getDescription()));
        if (!meta.getGroups().isEmpty()) {
            for (SubcommandGroupMeta group : meta.getGroups()) {
                SubcommandGroupData groupData = this.getSubcommandGroup(group);
                data.addSubcommandGroups(groupData);
            }
        } else if (!meta.getSubcommands().isEmpty()) {
            for (SubCommandMeta subcommand : meta.getSubcommands()) {
                SubcommandData subcommandData = this.getSubcommand(subcommand);
                data.addSubcommands(subcommandData);
            }
        } else {
            data.addOptions(this.toOptionData(meta.getOptions()));
        }
        return data;
    }

    private SubcommandGroupData getSubcommandGroup(SubcommandGroupMeta meta) {
        SubcommandGroupData data = new SubcommandGroupData(meta.getName(), this.toLabel(meta.getDescription()));
        for (SubCommandMeta command : meta.getSubcommands()) {
            data.addSubcommands(this.getSubcommand(command));
        }
        return data;
    }

    private SubcommandData getSubcommand(SubCommandMeta meta) {
        SubcommandData data = new SubcommandData(meta.getName(), this.toLabel(meta.getDescription()));
        data.addOptions(this.toOptionData(meta.getOptions()));
        return data;
    }

    private List<OptionData> toOptionData(List<CommandOption> meta) {
        return meta.stream().sorted((a, b) -> {
            if (a.isRequired() && b.isRequired()) {
                return 0;
            } else if (a.isRequired()) {
                return -1;
            } else {
                return 1;
            }
        }).map(this::toOptionData).collect(Collectors.toList());
    }

    private OptionData toOptionData(CommandOption meta) {
        OptionType type = this.getType(meta.getType());
        if (type == null) {
            throw new IllegalStateException("Invalid option type");
        }
        OptionData data = new OptionData(type, meta.getName(), this.toLabel(meta.getDescription()));
        data.setRequired(meta.isRequired());
        if (meta.getType() == OptType.ENUM) {
            meta.getEnumEntries().forEach(entry -> data.addChoice(this.toLabel(entry.getName()), entry.getId()));
        }
        return data;
    }

    private OptionType getType(OptType lindseyType) {
        switch (lindseyType) {
            // TODO: Wait num type for double
            case INT, DOUBLE, LONG -> {
                return OptionType.INTEGER;
            }
            case BOOLEAN -> {
                return OptionType.BOOLEAN;
            }
            case MEMBER, USER -> {
                return OptionType.USER;
            }
            case TEXT_CHANNEL, VOICE_CHANNEL -> {
                return OptionType.CHANNEL;
            }
            case ROLE -> {
                return OptionType.ROLE;
            }
            case REGEX, STRING, ENUM -> {
                return OptionType.STRING;
            }
        }
        return null;
    }

}
