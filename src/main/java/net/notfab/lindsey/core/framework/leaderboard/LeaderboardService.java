package net.notfab.lindsey.core.framework.leaderboard;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.profile.UserProfile;
import net.notfab.lindsey.core.repositories.mongo.LeaderboardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class LeaderboardService {

    private final ProfileManager profiles;
    private final LeaderboardRepository repository;

    public LeaderboardService(ProfileManager profiles, LeaderboardRepository repository) {
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
        String id = type.name() + ":" + user;
        Leaderboard leaderboard = repository.findById(id).orElse(new Leaderboard());
        leaderboard.setId(id);
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
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "count"));
        return repository.findAllByType(type, pageable);
    }

    public Leaderboard get(long user, LeaderboardType type) {
        return repository.findById(type.name() + ":" + user)
            .orElse(null);
    }

}
