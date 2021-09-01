package net.notfab.lindsey.core.listeners;

import lombok.extern.slf4j.Slf4j;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.entities.interaction.commands.CommandMetaBase;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.framework.command.CommandManager;
import net.notfab.lindsey.core.framework.command.MethodReference;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.core.service.EventService;
import org.jetbrains.annotations.NotNull;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class CommandListener implements Listener {

    private final CommandManager manager;
    private final PermissionManager permissions;
    private final Messenger msg;

    private final ThreadPoolTaskExecutor taskExecutor;

    public CommandListener(EventService events, CommandManager manager, PermissionManager permissions, Messenger msg) {
        this.manager = manager;
        this.permissions = permissions;
        this.msg = msg;
        events.addListener(this);
        taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setThreadNamePrefix("commands-");
        taskExecutor.initialize();
    }

    @EventHandler(ignoreCancelled = true)
    public void onSlashCommand(@NotNull ServerCommandEvent event) {
        CommandMetaBase meta = this.manager.findMeta(event.getPath());
        if (meta == null) {
            log.warn("Received invalid command: {}", event.getPath());
            return;
        }
        PermissionLevel access = this.manager.getPermission(event.getPath());
        if (!permissions.hasPermission(event.getMember(), access)) {
            this.msg.reply(event, Label.of("permissions.command"), true);
            return;
        } else if (!manager.hasListener(event.getPath())) {
            log.warn("Received unhandled command: {}", event.getPath());
            // TODO: send to network
            return;
        }
        MethodReference method = manager.getListener(event.getPath());
        try {
            this.taskExecutor.submit(() -> {
                try {
                    method.invoke(event);
                } catch (Exception ex) {
                    log.error("Failed to execute command", ex);
                }
            }).get(1500, TimeUnit.MILLISECONDS);
        } catch (ExecutionException | InterruptedException e) {
            log.error("Failed to schedule command execution", e);
        } catch (TimeoutException e) {
            log.warn("Timed out during command execution: {}", event.getPath());
            event.getUnderlying().deferReply(meta.isEphemeral())
                .queue();
        }
    }

}
