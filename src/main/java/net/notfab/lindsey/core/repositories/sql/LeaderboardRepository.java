package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.leaderboard.Leaderboard;
import net.notfab.lindsey.shared.enums.LeaderboardType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, Long> {

    Page<Leaderboard> findAllByType(LeaderboardType type, Pageable pageable);

    Optional<Leaderboard> findByTypeAndUser(LeaderboardType type, long user);

}
