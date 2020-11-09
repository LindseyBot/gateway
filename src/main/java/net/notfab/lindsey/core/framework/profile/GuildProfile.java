package net.notfab.lindsey.core.framework.profile;

import lombok.Data;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Lazy
@RedisHash("Lindsey:Profile")
public class GuildProfile {

    @Id
    private long owner;

    private String prefix;

}
