package net.notfab.lindsey.core.commands;

import net.lindseybot.controller.registry.ButtonRegistry;
import net.lindseybot.controller.registry.CommandRegistry;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.events.CommandMetaEvent;
import net.lindseybot.entities.interaction.buttons.ButtonMeta;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.builder.CommandBuilder;
import net.lindseybot.entities.interaction.commands.builder.SubCommandBuilder;
import net.lindseybot.entities.interaction.commands.builder.SubcommandGroupBuilder;
import net.lindseybot.enums.Modules;
import net.notfab.lindsey.core.framework.command.BotCommand;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.listeners.MetaListener;
import net.notfab.lindsey.core.service.Messenger;
import net.notfab.lindsey.core.service.Translator;
import org.springframework.stereotype.Component;

@Component
public class Developer extends Command {

    private final Messenger msg;
    private final Translator i18n;
    private final ButtonRegistry buttons;
    private final CommandRegistry commands;
    private final MetaListener metaListener;

    public Developer(Messenger msg, Translator i18n, ButtonRegistry buttons, CommandRegistry commands, MetaListener metaListener) {
        this.msg = msg;
        this.i18n = i18n;
        this.buttons = buttons;
        this.commands = commands;
        this.metaListener = metaListener;
    }

    @Override
    public CommandMeta getMetadata() {
        return new CommandBuilder("dev", Label.raw("Developer commands"), 1.0)
            .module(Modules.BASE)
            .privileged()
            .guilds(213044545825406976L)
            .addGroup(new SubcommandGroupBuilder("commands", Label.raw("Manages commands"))
                .addSubcommand(new SubCommandBuilder("reload", Label.raw("Reloads commands")).build())
                .addSubcommand(new SubCommandBuilder("redeploy", Label.raw("Redeploy all commands")).build())
                .addSubcommand(new SubCommandBuilder("list", Label.raw("Lists all registered commands")).build())
                .build())
            .addGroup(new SubcommandGroupBuilder("i18n", Label.raw("Manages i18n"))
                .addSubcommand(new SubCommandBuilder("reload", Label.raw("Reloads i18n")).build())
                .build())
            .addGroup(new SubcommandGroupBuilder("buttons", Label.raw("Manages buttons"))
                .addSubcommand(new SubCommandBuilder("reload", Label.raw("Reloads buttons")).build())
                .addSubcommand(new SubCommandBuilder("list", Label.raw("Lists all registered buttons")).build())
                .build())
            .build();
    }

    @BotCommand("dev/commands/reload")
    public void onReloadCommands(ServerCommandEvent event) {
        this.commands.fetchAll();
        this.msg.reply(event, Label.raw("Reloaded all commands."), true);
    }

    @BotCommand("dev/commands/redeploy")
    public void onRedeployCommands(ServerCommandEvent event) {
        for (CommandMeta meta : this.commands.getAll()) {
            CommandMetaEvent e = new CommandMetaEvent();
            e.setModel(meta);
            e.setCreate(true);
            this.metaListener.onCommandMeta(e);
        }
        this.msg.reply(event, Label.raw("Redeployed all commands."), true);
    }

    @BotCommand("dev/commands/list")
    public void onListCommands(ServerCommandEvent event) {
        StringBuilder builder = new StringBuilder();
        for (CommandMeta meta : this.commands.getAll()) {
            builder.append(", ").append(meta.getName());
        }
        this.msg.reply(event, Label.raw("Commands: " + builder.toString().replaceFirst(", ", "")), true);
    }

    @BotCommand("dev/i18n/reload")
    public void onReloadI18n(ServerCommandEvent event) {
        int loaded = this.i18n.reloadLanguages();
        this.msg.reply(event, Label.raw("Reloaded " + loaded + " languages."), true);
    }

    @BotCommand("dev/buttons/reload")
    public void onReloadButtons(ServerCommandEvent event) {
        this.buttons.fetchAll();
        this.msg.reply(event, Label.raw("Reloaded all buttons."), true);
    }

    @BotCommand("dev/buttons/list")
    public void onListButtons(ServerCommandEvent event) {
        StringBuilder builder = new StringBuilder();
        for (ButtonMeta meta : this.buttons.getAll()) {
            builder.append(", ").append(meta.getMethod());
        }
        this.msg.reply(event, Label.raw("Buttons: " + builder.toString().replaceFirst(", ", "")), true);
    }

}
