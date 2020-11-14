package net.notfab.lindsey.core.framework.leaderboard;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("Leaderboards")
public class Leaderboard {

    @Id
    private String id;

    private long user;
    private double count;
    private LeaderboardType type;

}
