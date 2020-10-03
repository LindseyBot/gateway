package net.notfab.lindsey.framework.leaderboard;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class LeaderboardService {

    public void update(long user, LeaderboardType type) {
        log.info("Would have updated the leaderboard (u=" + user + ",t=" + type.name() + ")");
    }

}
