package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.lindseybot.commands.Command;
import net.lindseybot.commands.request.CommandOption;
import net.lindseybot.commands.request.CommandRequest;
import net.lindseybot.commands.response.*;
import net.lindseybot.discord.Embed;
import net.notfab.lindsey.core.framework.DiscordAdapter;
import net.notfab.lindsey.core.framework.command.CommandRegistry;
import net.notfab.lindsey.core.framework.command.external.BadArgumentException;
import net.notfab.lindsey.core.framework.command.external.ExternalParser;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.menu.Menu;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.shared.enums.RabbitExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Service
public class ExternalCommandManager {

    private final IgnoreService ignores;
    private final PermissionManager permissions;
    private final Translator i18n;
    private final Messenger msg;
    private final CommandRegistry registry;
    private final RabbitTemplate template;
    private final DiscordAdapter adapter;

    private final ExecutorService service = Executors.newCachedThreadPool();
    private final ParameterizedTypeReference<CommandResponse> type = ParameterizedTypeReference.forType(CommandResponse.class);

    public ExternalCommandManager(IgnoreService ignores, PermissionManager permissions,
                                  Translator i18n, Messenger msg, CommandRegistry registry,
                                  RabbitTemplate template, DiscordAdapter adapter) {
        this.ignores = ignores;
        this.permissions = permissions;
        this.i18n = i18n;
        this.msg = msg;
        this.registry = registry;
        this.template = template;
        this.adapter = adapter;
    }

    /**
     * Listens for commands and fires network events.
     *
     * @param commandName Name of the attempted command.
     * @param args        List of all arguments.
     * @param member      Member who executed the command.
     * @param event       The message event.
     */
    public void onCommand(String commandName, List<String> args, Member member, GuildMessageReceivedEvent event) {
        Command command = this.registry.get(commandName);
        if (command == null) {
            return;
        }

        // -- Ignore check
        if (this.ignores.isIgnored(event.getGuild().getIdLong(), event.getChannel().getIdLong())) {
            return;
        }

        StringBuilder path = new StringBuilder()
            .append(commandName);

        // -- Find subcommand
        if (args.size() > 0 && !command.getCommands().isEmpty()) {
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

        if (command.isDeveloperOnly() && !Arrays.asList(87166524837613568L, 119566649731842049L).contains(member.getIdLong())) {
            // Dev-only commands
            return;
        }

        List<CommandOption> optList = command.getOptions();
        service.submit(() -> {
            CommandRequest request;
            try {
                request = ExternalParser.run(path, optList, new ArrayDeque<>(args), member, event);
            } catch (BadArgumentException ex) {
                msg.send(event.getChannel(), sender(member) + i18n.get(member, ex.getMessage(), ex.getArgs()));
                return;
            }
            request.setId(UUID.randomUUID().toString());
            request.setCommandPath("commands." + path);
            try {
                CommandResponse response =
                    template.convertSendAndReceiveAsType(RabbitExchange.COMMANDS.getName(), commandName, request, type);
                if (response == null || response instanceof InvalidResponse) {
                    return;
                } else if (response instanceof ErrorResponse) {
                    ErrorResponse error = (ErrorResponse) response;
                    event.getMessage().
                        reply(this.i18n.get(member, error.getMessage()))
                        .queue();
                    return;
                } else if (response instanceof MessageResponse) {
                    MessageResponse resp = (MessageResponse) response;
                    Message message = this.adapter.toMessage(resp.getMessage(), member);
                    event.getMessage()
                        .reply(message)
                        .queue();
                    return;
                } else if (response instanceof MenuResponse) {
                    MenuResponse menu = (MenuResponse) response;
                    List<MessageEmbed> pages = new ArrayList<>();
                    for (Embed embed : menu.getEmbeds()) {
                        this.adapter.buildEmbed(embed, member);
                    }
                    Menu.create(event.getChannel(), pages);
                    return;
                }
                log.warn("Unknown response " + (response.getClass().getSimpleName()) + ":" + response);
            } catch (Exception ex) {
                log.error("Error during command processing", ex);
            }
        });
    }

    private String sender(Member member) {
        return "**" + member.getEffectiveName() + "**: ";
    }

    private Command findCommand(List<String> args, Command command, StringBuilder path) throws BadArgumentException {
        do {
            if (args.isEmpty()) {
                // Subcommand not found
                throw new BadArgumentException("search.command", command.getName());
            }
            Optional<Command> sub = command.getCommands().stream()
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
        } while (!command.getCommands().isEmpty());
        return command;
    }

    public boolean isCommand(String commandName) {
        return this.registry.get(commandName) != null;
    }

}
