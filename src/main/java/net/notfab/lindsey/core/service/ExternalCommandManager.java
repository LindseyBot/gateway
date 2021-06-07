package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.lindseybot.controller.CommandMeta;
import net.lindseybot.controller.CommandOption;
import net.lindseybot.controller.registry.CommandRegistry;
import net.lindseybot.discord.bridge.InteractionData;
import net.lindseybot.framework.CommandRequest;
import net.lindseybot.services.MessagingService;
import net.notfab.lindsey.core.framework.command.external.BadArgumentException;
import net.notfab.lindsey.core.framework.command.external.ExternalParser;
import net.notfab.lindsey.core.framework.events.ServerMessageReceivedEvent;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ExternalCommandManager {

    private final PermissionManager permissions;
    private final Translator i18n;
    private final Messenger msg;

    private final CommandRegistry registry;
    private final MessagingService messaging;

    private final ExecutorService service = Executors.newCachedThreadPool();

    public ExternalCommandManager(PermissionManager permissions, Translator i18n, Messenger msg,
                                  CommandRegistry registry, MessagingService messaging) {
        this.permissions = permissions;
        this.i18n = i18n;
        this.msg = msg;
        this.registry = registry;
        this.messaging = messaging;
    }

    /**
     * Listens for commands and fires network events.
     *
     * @param commandName Name of the attempted command.
     * @param args        List of all arguments.
     * @param member      Member who executed the command.
     * @param event       The message event.
     */
    public void onCommand(String commandName, List<String> args, Member member, ServerMessageReceivedEvent event) {
        CommandMeta command = this.registry.get(commandName);
        if (command == null) {
            return;
        }

        StringBuilder path = new StringBuilder()
            .append(commandName);

        // -- Find subcommand
        if (args.size() > 0 && !command.getSubcommands().isEmpty()) {
            try {
                command = findCommand(args, command, path);
            } catch (BadArgumentException ex) {
                msg.send(event.getChannel(), sender(member) + i18n.get(member, ex.getMessage(), ex.getArgs()));
                return;
            }
        }

        // -- Permission check
        if (!this.permissions.hasPermission(member, !command.isAdminOnly(), "commands." + path)) {
            return;
        }

        if (command.isNsfw() && !event.getChannel().isNSFW()) {
            this.msg.send(event.getChannel(), this.i18n.get(member, "core.not_nsfw"));
            return;
        }

        // -- Dev-only commands
        if (command.isDeveloperOnly()
            && !Arrays.asList(87166524837613568L, 119566649731842049L).contains(member.getIdLong())) {
            return;
        }

        List<CommandOption> optList = command.getOptions();
        CommandMeta finalCommand = command;
        service.submit(() -> {
            CommandRequest request;
            try {
                request = ExternalParser.run(path, optList, new ArrayDeque<>(args), member, event);
            } catch (BadArgumentException ex) {
                msg.send(event.getChannel(), sender(member) + i18n.get(member, ex.getMessage(), ex.getArgs()));
                return;
            }
            request.setPath(path.toString());

            InteractionData data = new InteractionData();
            data.setGuildId(member.getGuild().getIdLong());
            data.setChannelId(event.getChannel().getIdLong());
            data.setToken(null);
            data.setMessageId(event.getMessage().getIdLong());
            data.setUserId(event.getMessage().getAuthor().getIdLong());
            request.setInteraction(data);
            this.messaging.enqueue("Lindsey:Commands:" + finalCommand.getName(), request);
        });
    }

    private String sender(Member member) {
        return "**" + member.getEffectiveName() + "**: ";
    }

    private CommandMeta findCommand(List<String> args, CommandMeta command, StringBuilder path) throws BadArgumentException {
        do {
            if (args.isEmpty()) {
                // Subcommand not found
                throw new BadArgumentException("search.command", command.getName());
            }
            Optional<CommandMeta> sub = command.getSubcommands().stream()
                .filter(cmd -> cmd.getName().equals(args.get(0)))
                .findFirst();
            if (sub.isEmpty()) {
                // Subcommand not found
                throw new BadArgumentException("search.command", args.get(0));
            } else {
                command = sub.get();
                path.append(".").append(command.getName());
                args.remove(0);
            }
        } while (!command.getSubcommands().isEmpty());
        return command;
    }

    public boolean isCommand(String commandName) {
        return this.registry.get(commandName) != null;
    }

}
