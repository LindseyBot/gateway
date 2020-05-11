package net.notfab.lindsey.core;

import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.discord.CommandListener;
import net.notfab.lindsey.framework.command.CommandManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Lindsey {

    private static final Logger logger = LoggerFactory.getLogger(Lindsey.class);

    private final ShardManager shardManager;
    private final CommandManager commandManager;

    protected Lindsey(ShardManager shardManager, CommandManager commandManager) {
        this.shardManager = shardManager;
        this.commandManager = commandManager;
    }

    public void init() {
        this.shardManager.addEventListener(new CommandListener(this.commandManager));
        logger.info("Boat is now Floating!");
    }

}
