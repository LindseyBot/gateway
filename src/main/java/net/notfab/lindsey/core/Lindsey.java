package net.notfab.lindsey.core;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class Lindsey {

    private final ShardManager shardManager;

    protected Lindsey(ShardManager shardManager) {
        this.shardManager = shardManager;
    }

    public void init() {
        log.info("Boat is now Floating!");
    }

    public void addEventListener(ListenerAdapter listener) {
        this.shardManager.addEventListener(listener);
    }

}
