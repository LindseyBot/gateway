package net.notfab.lindsey.framework.profile;

import lombok.Data;
import net.notfab.lindsey.framework.i18n.Language;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@Data
@Lazy
@RedisHash("Lindsey:Profile")
public class UserProfile {

    @Id
    private long owner;

    private Language language = Language.en_US;

    private long cookies = 0;

}
