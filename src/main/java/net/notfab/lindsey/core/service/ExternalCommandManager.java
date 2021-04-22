package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.notfab.lindsey.core.framework.command.CommandRegistry;
import net.notfab.lindsey.core.framework.command.external.BadArgumentException;
import net.notfab.lindsey.core.framework.command.external.ExternalParser;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.shared.entities.commands.CommandOption;
import net.notfab.lindsey.shared.entities.commands.CommandRequest;
import net.notfab.lindsey.shared.entities.commands.CommandResponse;
import net.notfab.lindsey.shared.entities.commands.ExternalCommand;
import net.notfab.lindsey.shared.entities.commands.response.ErrorResponse;
import net.notfab.lindsey.shared.entities.commands.response.InvalidResponse;
import net.notfab.lindsey.shared.enums.RabbitExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Optional;
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

    private final ExecutorService service = Executors.newCachedThreadPool();
    private final ParameterizedTypeReference<CommandResponse> type = ParameterizedTypeReference.forType(CommandResponse.class);

    public ExternalCommandManager(IgnoreService ignores, PermissionManager permissions,
                                  Translator i18n, Messenger msg, CommandRegistry registry,
                                  RabbitTemplate template) {
        this.ignores = ignores;
        this.permissions = permissions;
        this.i18n = i18n;
        this.msg = msg;
        this.registry = registry;
        this.template = template;
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
        ExternalCommand command = this.registry.get(commandName);
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
                msg.send(event.getChannel(), sender(member) + i18n.get(member, ex.getMessage()));
                return;
            }
        }

        // -- Permission check
        if (!this.permissions.hasPermission(member, "commands." + path)) {
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
                }
                event.getChannel()
                    .sendMessage(response.toString())
                    .queue();
            } catch (Exception ex) {
                log.error("Error during command processing", ex);
            }
        });
    }

    private String sender(Member member) {
        return "**" + member.getEffectiveName() + "**: ";
    }

    private ExternalCommand findCommand(List<String> args, ExternalCommand command, StringBuilder path) throws BadArgumentException {
        do {
            if (args.isEmpty()) {
                // Subcommand not found
                throw new BadArgumentException("search.command", command.getName());
            }
            Optional<ExternalCommand> sub = command.getCommands().stream()
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

}
