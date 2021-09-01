package net.notfab.lindsey.core.framework.command;

import lombok.extern.slf4j.Slf4j;
import net.lindseybot.controller.registry.CommandRegistry;
import net.lindseybot.entities.interaction.commands.CommandMeta;
import net.lindseybot.entities.interaction.commands.SubCommandMeta;
import net.lindseybot.entities.interaction.commands.SubcommandGroupMeta;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class CommandManager {

    private final CommandRegistry registry;
    private final Map<String, Method> listeners = new HashMap<>();

    public CommandManager(List<Command> commands, CommandRegistry registry) {
        this.registry = registry;
        commands.forEach(this::register);
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
            listeners.put(cmd.value(), method);
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

    public Method getListener(String path) {
        return this.listeners.get(path);
    }

    public PermissionLevel getPermission(String path) {
        CommandMeta meta = this.findCommand(path);
        if (!path.contains("/")) {
            return meta.getPermission();
        }
        String[] split = path.split("/");
        if (split.length == 2) {
            SubCommandMeta data = meta.getSubcommands().stream()
                .filter(g -> g.getName().equals(split[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid command"));
            return data.getPermission();
        } else {
            SubcommandGroupMeta data = meta.getGroups().stream()
                .filter(g -> g.getName().equals(split[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid command"));
            SubCommandMeta subcommand = data.getSubcommands().stream()
                .filter(g -> g.getName().equals(split[1]))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Invalid command"));
            return subcommand.getPermission();
        }
    }

}
