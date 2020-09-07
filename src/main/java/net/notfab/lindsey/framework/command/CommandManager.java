package net.notfab.lindsey.framework.command;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommandManager {

    @Getter
    private static CommandManager Instance;

    @Getter
    private final TaskExecutor pool;
    private final Map<String, Command> commandList = new HashMap<>();

    public CommandManager(@Qualifier("commands") TaskExecutor pool, List<Command> commands) {
        Instance = this;
        this.pool = pool;
        commands.forEach(this::register);
    }

    private void register(Command command) {
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
