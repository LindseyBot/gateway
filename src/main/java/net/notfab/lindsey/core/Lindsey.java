package net.notfab.lindsey.core;

import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.discord.CommandListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lindsey {

    private static final Logger logger = LoggerFactory.getLogger(Lindsey.class);
    private final ShardManager shardManager;

    public Lindsey(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public void init() {
        this.shardManager.addEventListener(new CommandListener());
        logger.info("Boat is now Floating!");
    }

}
