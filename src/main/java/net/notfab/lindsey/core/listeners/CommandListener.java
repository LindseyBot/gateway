package net.notfab.lindsey.core.listeners;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.eventti.ListenerPriority;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandManager;
import net.notfab.lindsey.core.framework.events.ServerMessageReceivedEvent;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.service.EventService;
import net.notfab.lindsey.core.service.ExternalCommandManager;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class CommandListener implements Listener {

    private final Pattern argPattern = Pattern.compile("(?:([^\\s\"]+)|\"((?:\\w+|\\\\\"|[^\"])+)\")");

    private final ProfileManager profiles;
    private final CommandManager manager;
    private final PermissionManager permissions;
    private final TaskExecutor threadPool;
    private final ExternalCommandManager externalCommandManager;

    public CommandListener(EventService events, CommandManager manager, ProfileManager profileManager,
                           PermissionManager permissions, ExternalCommandManager externalCommandManager) {
        this.manager = manager;
        this.threadPool = manager.getPool();
        this.profiles = profileManager;
        this.permissions = permissions;
        this.externalCommandManager = externalCommandManager;
        events.addListener(this);
    }

    @EventHandler(priority = ListenerPriority.HIGHEST)
    public void onMessageReceived(ServerMessageReceivedEvent event) {
        String rawMessage = event.getMessage().getContentRaw();
        if (rawMessage.split("\\s+").length == 0) {
            return;
        }

        String prefix = this.findPrefix(rawMessage.split("\\s+")[0].toLowerCase(), event.getGuild(), event.getGuild().getSelfMember());
        if (prefix == null) {
            return;
        }

        // -- Argument Finder
        List<String> arguments = this.getArguments(event.getMessage().getContentDisplay().substring(prefix.length()));
        if (arguments.isEmpty()) {
            event.getChannel().sendMessage("Hmm?").queue();
            event.setCancelled(true);
            return;
        }

        String commandName = arguments.get(0).toLowerCase();
        if (arguments.size() == 1) {
            arguments.clear();
        } else {
            arguments.remove(0);
        }

        // -- Override
        if (this.externalCommandManager.isCommand(commandName)) {
            event.setCancelled(true);
            this.externalCommandManager.onCommand(commandName, arguments, event.getMember(), event);
            return;
        }

        // -- Execution
        Command command = this.manager.findCommand(commandName);
        if (command == null) {
            return;
        }

        // -- Permission check
        if (!this.permissions.hasPermission(event.getMember(), "commands." + command.getInfo().getName())) {
            return;
        }

        event.setCancelled(true);
        threadPool.execute(() -> {
            try {
                Bundle bundle = new Bundle();
                command.execute(event.getMember(), event.getChannel(), arguments.toArray(new String[0]), event.getMessage(), bundle);
            } catch (Exception ex) {
                log.error("Error during command execution", ex);
            }
        });
    }

    private String findPrefix(String message, Guild guild, Member self) {
        if (message.startsWith("l!")) {
            return "l!";
        } else if (message.startsWith(self.getAsMention()) || message.startsWith("<@!" + self.getIdLong() + ">")) {
            return "@" + self.getEffectiveName();
        } else {
            ServerProfile profile = profiles.get(guild);
            String prefix = profile.getPrefix();
            if (prefix == null || prefix.isBlank()) {
                prefix = "L!";
            }
            if (message.startsWith(prefix)) {
                return prefix;
            }
        }
        return null;
    }

    private List<String> getArguments(String rawArgs) {
        List<String> args = new ArrayList<>();
        Matcher m = argPattern.matcher(rawArgs);
        while (m.find()) {
            if (m.group(1) == null) {
                args.add(m.group(2));
            } else {
                args.add(m.group(1));
            }
        }
        return args;
    }

}
