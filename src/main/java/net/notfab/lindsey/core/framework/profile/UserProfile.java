package net.notfab.lindsey.core.framework.profile;

import lombok.Data;
import net.notfab.lindsey.core.framework.i18n.Language;
import net.notfab.lindsey.core.framework.inventory.enums.Flags;
import net.notfab.lindsey.core.framework.inventory.enums.Items;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Data
@Document(collection = "UserProfiles")
public class UserProfile {

    @Id
    private String owner;

    private String name;
    private long lastSeen;
    private Language language = Language.en_US;

    @Enumerated(EnumType.STRING)
    private Items background;

    @Enumerated(EnumType.STRING)
    private Flags country;

    private long cookies = 0;

    private long cookieStreak = 0;
    private long lastDailyCookies = 0;

}
