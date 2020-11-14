package net.notfab.lindsey.core.framework.profile.guild;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "ReactionRoles")
public class ReactionRole {

    @Id
    private String id;
    private String name;

    private long roleId;
    private long guildId;
    private long messageId;
    private long channelId;

    private String reaction;

}
