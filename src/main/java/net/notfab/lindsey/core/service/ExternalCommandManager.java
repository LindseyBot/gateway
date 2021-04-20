package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.notfab.lindsey.core.framework.command.external.BadArgumentException;
import net.notfab.lindsey.core.framework.command.external.ExternalParser;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.shared.entities.commands.CommandData;
import net.notfab.lindsey.shared.entities.commands.CommandOption;
import net.notfab.lindsey.shared.entities.commands.ExternalCommand;
import net.notfab.lindsey.shared.entities.commands.OptionType;
import net.notfab.lindsey.shared.entities.commands.builders.ExternalCommandBuilder;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ExternalCommandManager {

    private final IgnoreService ignores;
    private final PermissionManager permissions;
    private final Translator i18n;
    private final Messenger msg;
    private final JedisPool redis;
    private final Map<String, ExternalCommand> commands = new HashMap<>();

    private final ExecutorService service = Executors.newCachedThreadPool();

    public ExternalCommandManager(IgnoreService ignores, PermissionManager permissions, Translator i18n, Messenger msg, JedisPool redis) {
        this.ignores = ignores;
        this.permissions = permissions;
        this.i18n = i18n;
        this.msg = msg;
        this.redis = redis;
        this.onRegister(new ExternalCommandBuilder("testd", "description")
            .subCommand("users", "userdescription")
            .option(OptionType.MEMBER, "targets", "targets").required().list().build()
            .next()
            .subCommand("groups", "groupsdesc")
            .option(OptionType.STRING, "str", "str").required().build()
            .next()
            .build());
    }

    @EventListener
    public void onUnregister(ExternalCommand command) {
        try (Jedis jedis = this.redis.getResource()) {
            final String key = "Lindsey:CommandRegistry";
            if (!jedis.hexists(key, command.getName().toLowerCase())) {
                return;
            }
            int count = Integer.parseInt(jedis.hget(key, command.getName().toLowerCase()));
            if (count == 0) {
                this.commands.remove(command.getName().toLowerCase());
                command.getAliases()
                    .forEach(name -> this.commands.remove(name.toLowerCase()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean exists(String commandName) {
        try (Jedis jedis = this.redis.getResource()) {
            return jedis.hexists("Lindsey:CommandRegistry", commandName);
        } catch (Exception e) {
            return false;
        }
    }

    private ExternalCommand getCommand(String commandName) {
        commandName = commandName.toLowerCase();
        if (commands.containsKey(commandName)) {
            return this.commands.get(commandName);
        }
        ExternalCommand command = (ExternalCommand) this.redis.opsForHash().get("Lindsey:Registry", commandName);
        if (command == null) {
            return null;
        }
        this.commands.put(commandName, command);
        for (String alias : command.getAliases()) {
            this.commands.put(alias.toLowerCase(), command);
        }
        return command;
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
        if (!this.exists(commandName)) {
            return;
        }
        ExternalCommand command = this.getCommand(commandName);
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
            CommandData data;
            try {
                data = ExternalParser.run(path, optList, new ArrayDeque<>(args), member, event);
            } catch (BadArgumentException ex) {
                msg.send(event.getChannel(), sender(member) + i18n.get(member, ex.getMessage(), ex.getArgs()));
                return;
            }
            event.getChannel().sendMessage("executed").queue();
            System.out.println(data);
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
