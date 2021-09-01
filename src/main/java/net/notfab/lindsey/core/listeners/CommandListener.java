package net.notfab.lindsey.core.listeners;

import lombok.extern.slf4j.Slf4j;
import net.lindseybot.entities.discord.Label;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.eventti.EventHandler;
import net.notfab.eventti.Listener;
import net.notfab.lindsey.core.framework.command.CommandManager;
import net.notfab.lindsey.core.framework.events.ServerCommandEvent;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.core.service.EventService;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
@Component
public class CommandListener implements Listener {

    private final CommandManager manager;
    private final PermissionManager permissions;
    private final Messenger msg;

    public CommandListener(EventService events, CommandManager manager, PermissionManager permissions, Messenger msg) {
        this.manager = manager;
        this.permissions = permissions;
        this.msg = msg;
        events.addListener(this);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSlashCommand(@NotNull ServerCommandEvent event) {
        if (!manager.hasListener(event.getPath())) {
            log.warn("Received request for invalid command");
            return;
        }
        PermissionLevel access = this.manager.getPermission(event.getPath());
        if (!this.permissions.hasPermission(event.getMember(), access)) {
            this.msg.reply(event, Label.raw("No permission to use this command."), true);
            return;
        }
        Method method = manager.getListener(event.getPath());
        try {
            method.invoke(event);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            log.error("Failed to execute command", ex);
        }
    }

}
