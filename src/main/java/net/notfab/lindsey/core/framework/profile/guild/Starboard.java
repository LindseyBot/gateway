package net.notfab.lindsey.core.framework.profile.guild;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("Starboard")
public class Starboard {

    @Id
    private String id;

    private long guildId;
    private long channelId;
    private Long starboardMessageId;
    private int stars;

}
