package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.profile.member.Strike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StrikeRepository extends JpaRepository<Strike, Long> {

    @Query("SELECT sum(s.count) from Strike s WHERE s.user  = ?1 and s.guild = ?2")
    int sumByUserAndGuild(long user, long guild);

}
