package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.Member;
import net.notfab.lindsey.core.framework.events.StrikeReceivedEvent;
import net.notfab.lindsey.shared.entities.profile.member.Strike;
import net.notfab.lindsey.shared.repositories.sql.StrikeRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.springframework.stereotype.Service;

@Service
public class StrikeService {

    private final Snowflake snowflake;
    private final StrikeRepository repository;
    private final EventService events;

    public StrikeService(Snowflake snowflake, StrikeRepository repository, EventService events) {
        this.snowflake = snowflake;
        this.repository = repository;
        this.events = events;
    }

    public int count(long user, long guild) {
        return repository.sumByUserAndGuild(user, guild);
    }

    public void strike(Member member, int count, String reason) {
        Strike strike = new Strike();
        strike.setId(snowflake.next());
        strike.setUser(member.getIdLong());
        strike.setGuild(member.getGuild().getIdLong());
        strike.setCount(count);
        strike.setAdmin(0L);
        strike.setReason(reason);
        this.repository.save(strike);

        // Fire async
        StrikeReceivedEvent event = new StrikeReceivedEvent();
        event.setStrike(strike);
        event.setTotal(this.count(member.getIdLong(), member.getGuild().getIdLong()));
        this.events.fire(event);
    }

}
