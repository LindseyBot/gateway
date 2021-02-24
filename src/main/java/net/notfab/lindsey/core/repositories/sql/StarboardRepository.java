package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.Starboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StarboardRepository extends JpaRepository<Starboard, Long> {

    Optional<Starboard> findByStarboardMessageId(long messageId);

}
