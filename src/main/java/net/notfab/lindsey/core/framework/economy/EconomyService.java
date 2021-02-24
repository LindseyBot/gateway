package net.notfab.lindsey.core.framework.economy;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.leaderboard.LeaderboardService;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.UserProfile;
import net.notfab.lindsey.shared.enums.LeaderboardType;
import org.springframework.stereotype.Service;

@Service
public class EconomyService {

    private final ProfileManager profiles;
    private final LeaderboardService leaderboards;

    public EconomyService(ProfileManager profiles, LeaderboardService leaderboards) {
        this.profiles = profiles;
        this.leaderboards = leaderboards;
    }

    public boolean has(Member member, long count) {
        return has(member.getUser(), count);
    }

    public boolean has(User user, long count) {
        return has(user.getIdLong(), count);
    }

    public boolean has(long user, long count) {
        UserProfile profile = profiles.getUser(user);
        return profile.getCookies() >= count;
    }

    public void pay(Member member, long count) {
        this.pay(member.getUser(), count);
    }

    public void pay(User user, long count) {
        this.pay(user.getIdLong(), count);
    }

    public void pay(long user, long count) {
        UserProfile profile = profiles.getUser(user);
        profile.setCookies(profile.getCookies() + count);
        profiles.save(profile);
        leaderboards.update(user, LeaderboardType.COOKIES);
    }

    public void deduct(Member member, long count) {
        this.deduct(member.getUser(), count);
    }

    public void deduct(User user, long count) {
        this.deduct(user.getIdLong(), count);
    }

    public void deduct(long user, long count) {
        UserProfile profile = profiles.getUser(user);
        if (profile.getCookies() < count) {
            throw new IllegalArgumentException("commands.economy.not_enough");
        }
        profile.setCookies(profile.getCookies() - count);
        profiles.save(profile);
        leaderboards.update(user, LeaderboardType.COOKIES);
    }

}
