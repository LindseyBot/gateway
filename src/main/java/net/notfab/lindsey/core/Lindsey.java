package net.notfab.lindsey.core;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.discord.CommandListener;
import net.notfab.lindsey.core.framework.command.CommandManager;
import net.notfab.lindsey.core.framework.permissions.PermissionManager;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.waiter.Waiter;
import net.notfab.lindsey.core.service.ExternalCommandManager;
import net.notfab.lindsey.core.service.IgnoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Lindsey {

    private static final Logger logger = LoggerFactory.getLogger(Lindsey.class);

    private final ShardManager shardManager;
    private final CommandManager commandManager;
    private final ProfileManager profileManager;
    private final PermissionManager permissionManager;
    private final IgnoreService ignoreService;
    private final Waiter waiter;
    private final ExternalCommandManager externalCommandManager;

    protected Lindsey(ShardManager shardManager, CommandManager commandManager,
                      ProfileManager profileManager, PermissionManager permissionManager,
                      IgnoreService ignoreService, Waiter waiter,
                      ExternalCommandManager externalCommandManager) {
        this.shardManager = shardManager;
        this.commandManager = commandManager;
        this.profileManager = profileManager;
        this.permissionManager = permissionManager;
        this.ignoreService = ignoreService;
        this.waiter = waiter;
        this.externalCommandManager = externalCommandManager;
    }

    public void init() {
        this.shardManager.addEventListener(new CommandListener(this.commandManager,
            this.profileManager, this.permissionManager, this.ignoreService, externalCommandManager));
        this.shardManager.addEventListener(waiter);
        this.permissionManager.init();
        logger.info("Boat is now Floating!");
    }

    public void addEventListener(ListenerAdapter listener) {
        this.shardManager.addEventListener(listener);
    }

}
