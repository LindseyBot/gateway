package net.notfab.lindsey.framework.command;

import net.notfab.lindsey.core.commands.fun.Anime;
import net.notfab.lindsey.core.commands.fun.Color;
import net.notfab.lindsey.core.commands.fun.Flip;
import net.notfab.lindsey.core.commands.fun.Roll;
import net.notfab.lindsey.core.commands.nsfw.Danbooru;
import net.notfab.lindsey.core.commands.nsfw.Rule34;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommandManager {

    private final Map<String, Command> commandList = new HashMap<>();

    public CommandManager() {
        this.register(new Color());
        this.register(new Flip());
        this.register(new Anime());
        this.register(new Rule34());
        this.register(new Danbooru());
        this.register(new Roll());
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
