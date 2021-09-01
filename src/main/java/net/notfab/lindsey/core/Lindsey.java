package net.notfab.lindsey.core;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.notfab.lindsey.core.framework.waiter.Waiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class Lindsey {

    private static final Logger logger = LoggerFactory.getLogger(Lindsey.class);

    private final ShardManager shardManager;
    private final Waiter waiter;

    protected Lindsey(ShardManager shardManager, Waiter waiter) {
        this.shardManager = shardManager;
        this.waiter = waiter;
    }

    public void init() {
        this.shardManager.addEventListener(waiter);
        logger.info("Boat is now Floating!");
    }

    public void addEventListener(ListenerAdapter listener) {
        this.shardManager.addEventListener(listener);
    }

}
