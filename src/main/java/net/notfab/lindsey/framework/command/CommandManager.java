package net.notfab.lindsey.framework.command;

import lombok.Getter;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class CommandManager {

    @Getter
    private static CommandManager Instance;

    @Getter
    private final TaskExecutor pool;
    private final ApplicationContext context;
    private final Map<String, Command> commandList = new HashMap<>();

    public CommandManager(@Qualifier("commands") TaskExecutor pool, ApplicationContext context) {
        Instance = this;
        this.pool = pool;
        this.context = context;
        this.findCommands().forEach(this::register);
    }

    private Set<Class<? extends Command>> findCommands() {
        Reflections reflections = new Reflections("net.notfab.lindsey.core.commands");
        return reflections.getSubTypesOf(Command.class);
    }

    private void register(Class<? extends Command> clazz) {
        Command command = context.getBean(clazz);
        CommandDescriptor descriptor = command.getInfo();
        this.commandList.put(descriptor.getName(), command);
        for (String alias : descriptor.getAliases()) {
            this.commandList.put(alias, command);
        }
    }

    public Command findCommand(String name) {
        return commandList.get(name);
    }

    public Collection<Command> getCommands() {
        return this.commandList.values();
    }

}
