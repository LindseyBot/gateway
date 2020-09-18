package net.notfab.lindsey.core;

import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.discord.CommandListener;
import net.notfab.lindsey.core.service.IgnoreService;
import net.notfab.lindsey.framework.command.CommandManager;
import net.notfab.lindsey.framework.menu.MenuManager;
import net.notfab.lindsey.framework.permissions.PermissionManager;
import net.notfab.lindsey.framework.profile.ProfileManager;
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

    protected Lindsey(ShardManager shardManager, CommandManager commandManager,
                      ProfileManager profileManager, PermissionManager permissionManager, IgnoreService ignoreService) {
        this.shardManager = shardManager;
        this.commandManager = commandManager;
        this.profileManager = profileManager;
        this.permissionManager = permissionManager;
        this.ignoreService = ignoreService;
    }

    public void init() {
        this.shardManager.addEventListener(new CommandListener(this.commandManager,
            this.profileManager, this.permissionManager, this.ignoreService));
        this.permissionManager.init();
        logger.info("Boat is now Floating!");
    }

    public Lindsey addEventListener(MenuManager menuManager) {
        this.shardManager.addEventListener(menuManager);
        return this;
    }

}
