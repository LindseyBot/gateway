package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.profile.member.Strike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StrikeRepository extends JpaRepository<Strike, Long> {

    int sumByUserAndGuild(long user, long guild);

}
