package net.notfab.lindsey.core.framework.profile;

import lombok.Data;
import net.notfab.lindsey.core.framework.profile.member.RoleHistory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "MemberProfiles")
public class MemberProfile {

    @Id
    private String id;
    private long guildId;
    private long userId;

    private long lastSeen;
    private int strikes = 0;

    private RoleHistory roleHistory = new RoleHistory();

}
