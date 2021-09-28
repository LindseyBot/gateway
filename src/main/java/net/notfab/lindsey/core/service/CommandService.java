package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.lindseybot.controller.registry.CommandRegistry;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.CommandMetaBase;
import net.lindseybot.entities.interaction.commands.SubcommandGroupMeta;
import net.notfab.lindsey.core.framework.command.BotCommand;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.MethodReference;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CommandService {

    private final CommandRegistry registry;
    private final Map<String, MethodReference> listeners = new HashMap<>();

    public CommandService(List<Command> commands, CommandRegistry registry) {
        this.registry = registry;
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.schedule(() -> {
            try {
                this.registry.fetchAll();
                commands.forEach(this::register);
                log.info("Loaded {} commands.", this.registry.getAll().size());
            } catch (Exception ex) {
                log.error("Error during command registration", ex);
            }
        }, 15, TimeUnit.SECONDS);
        service.shutdown();
    }

    private void register(Command command) {
        CommandMeta metadata = command.getMetadata();
        if (metadata != null) {
            this.registry.register(metadata);
        }
        for (Method method : command.getClass().getDeclaredMethods()) {
            BotCommand cmd = method.getDeclaredAnnotation(BotCommand.class);
            if (cmd == null) {
                continue;
            } else if (method.getParameterCount() == 0) {
                log.warn("Invalid command listener declaration: " + cmd.value());
                continue;
            } else if (!ServerCommandEvent.class.equals(method.getParameterTypes()[0])) {
                log.warn("Invalid command listener declaration: " + cmd.value());
                continue;
            }
            listeners.put(cmd.value(), new MethodReference(command, method));
        }
    }

    public boolean hasListener(String path) {
        return this.listeners.containsKey(path);
    }

    public CommandMeta findCommand(String path) {
        if (path.contains("/")) {
            return this.registry.get(path.split("/")[0]);
        } else {
            return this.registry.get(path);
        }
    }

    public Collection<CommandMeta> getCommands() {
        return this.registry.getAll();
    }

    public MethodReference getListener(String path) {
        return this.listeners.get(path);
    }

    public CommandMetaBase findMeta(String path) {
        String[] split = path.split("/");
        CommandMeta command = this.findCommand(path);
        if (command == null || split.length == 1) {
            return command;
        } else if (split.length == 2) {
            return command.getSubcommands().stream()
                .filter(g -> g.getName().equals(split[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid command"));
        } else {
            SubcommandGroupMeta group = command.getGroups().stream()
                .filter(g -> g.getName().equals(split[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid command"));
            return group.getSubcommands().stream()
                .filter(g -> g.getName().equals(split[2]))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid command"));
        }
    }

}
