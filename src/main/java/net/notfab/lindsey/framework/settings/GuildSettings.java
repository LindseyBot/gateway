package net.notfab.lindsey.framework.settings;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RedisHash("Lindsey:Settings")
public class GuildSettings {

    @Id
    private long owner;

}
