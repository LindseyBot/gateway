package net.notfab.lindsey.core.discord;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandManager;
import net.notfab.lindsey.framework.permissions.PermissionManager;
import net.notfab.lindsey.framework.profile.GuildProfile;
import net.notfab.lindsey.framework.profile.ProfileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(CommandListener.class);
    private final Pattern argPattern = Pattern.compile("(?:([^\\s\"]+)|\"((?:\\w+|\\\\\"|[^\"])+)\")");

    private final ProfileManager profiles;
    private final CommandManager manager;
    private final PermissionManager permissions;
    private final TaskExecutor threadPool;

    public CommandListener(CommandManager manager, ProfileManager profileManager, PermissionManager permissions) {
        this.manager = manager;
        this.threadPool = manager.getPool();
        this.profiles = profileManager;
        this.permissions = permissions;
    }

    @Override
    public void onGuildMessageReceived(@Nonnull GuildMessageReceivedEvent event) {
        Member member = event.getMember();
        if (member == null || event.getAuthor().isBot() || event.isWebhookMessage()) {
            return;
        }
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
            return;
        }
        String commandName = arguments.get(0).toLowerCase();
        if (arguments.size() == 1) {
            arguments.clear();
        } else {
            arguments.remove(0);
        }
        // -- Execution
        Command command = this.manager.findCommand(commandName);
        if (command == null) {
            return;
        }
        // -- Permission check
        if (!this.permissions.hasPermission(member, "commands." + command.getInfo().getName())) {
            return;
        }
        threadPool.execute(() -> {
            try {
                Bundle bundle = new Bundle();
                command.execute(member, event.getChannel(), arguments.toArray(new String[0]), event.getMessage(), bundle);
            } catch (Exception ex) {
                logger.error("Error during command execution", ex);
            }
        });
    }

    private String findPrefix(String message, Guild guild, Member self) {
        if (message.startsWith("l!")) {
            return "l!";
        } else if (message.startsWith(self.getAsMention()) || message.startsWith("<@!119482224713269248>")) {
            return "@" + self.getEffectiveName();
        } else {
            GuildProfile profile = profiles.get(guild);
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
