package net.notfab.lindsey.core.service;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.repositories.sql.LeaderboardRepository;
import net.notfab.lindsey.shared.entities.leaderboard.Leaderboard;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import net.notfab.lindsey.shared.enums.LeaderboardType;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LeaderboardService {

    private final Snowflake snowflake;
    private final ProfileManager profiles;
    private final LeaderboardRepository repository;

    public LeaderboardService(Snowflake snowflake, ProfileManager profiles, LeaderboardRepository repository) {
        this.snowflake = snowflake;
        this.profiles = profiles;
        this.repository = repository;
    }

    public void update(Member member, LeaderboardType type) {
        this.update(member.getUser(), type);
    }

    public void update(User user, LeaderboardType type) {
        this.update(user.getIdLong(), type);
    }

    public void update(long user, LeaderboardType type) {
        Leaderboard leaderboard = repository.findByTypeAndUser(type, user)
            .orElse(new Leaderboard());
        if(leaderboard.getId() == 0){
            leaderboard.setId(snowflake.next());
        }
        leaderboard.setType(type);
        leaderboard.setUser(user);
        if (type == LeaderboardType.COOKIES) {
            UserProfile profile = profiles.getUser(user);
            leaderboard.setCount(profile.getCookies());
            repository.save(leaderboard);
        } else if (type == LeaderboardType.SLOT_WINS) {
            leaderboard.setCount(leaderboard.getCount() + 1);
        } else {
            log.info("Would have updated the leaderboard (u=" + user + ",t=" + type.name() + ")");
        }
    }

    public Page<Leaderboard> getLeaderboard(LeaderboardType type, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "count"));
        return repository.findAllByType(type, pageable);
    }

    public Leaderboard get(long user, LeaderboardType type) {
        return repository.findByTypeAndUser(type, user)
            .orElse(null);
    }

}
