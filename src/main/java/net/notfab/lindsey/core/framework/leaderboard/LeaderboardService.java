package net.notfab.lindsey.core.framework.leaderboard;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LeaderboardService {

    public void update(Member member, LeaderboardType type) {
        this.update(member.getUser(), type);
    }

    public void update(User user, LeaderboardType type) {
        this.update(user.getIdLong(), type);
    }

    public void update(long user, LeaderboardType type) {
        log.info("Would have updated the leaderboard (u=" + user + ",t=" + type.name() + ")");
    }

}
