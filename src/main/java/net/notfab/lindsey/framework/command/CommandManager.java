package net.notfab.lindsey.framework.command;

import net.notfab.lindsey.core.commands.Color;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandManager {

    private final Map<String, Command> commandList = new HashMap<>();

    public CommandManager() {
        this.register(new Color());
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

}
