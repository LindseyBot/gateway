package net.notfab.lindsey.framework.settings;

import lombok.Data;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@RedisHash("Lindsey:Settings")
public class UserSettings {

    @Id
    private long owner;

    @Lazy
    private boolean test = false;

}
