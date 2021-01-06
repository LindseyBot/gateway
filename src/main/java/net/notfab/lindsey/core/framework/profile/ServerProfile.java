package net.notfab.lindsey.core.framework.profile;

import lombok.Data;
import net.notfab.lindsey.core.framework.i18n.Language;
import net.notfab.lindsey.core.framework.models.PlayList;
import net.notfab.lindsey.core.framework.models.PlayListCursor;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.redis.core.RedisHash;

@Data
@Lazy
@RedisHash("Lindsey:Profile")
public class ServerProfile {

    @Id
    private long owner;

    private String prefix;
    private PlayListCursor cursor;
    private Language language;

    @DBRef
    private PlayList activePlayList;

    private boolean keepRolesEnabled;
    private Long starboardChannelId;

}
