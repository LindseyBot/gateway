package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.shared.entities.Starboard;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface StarboardRepository extends MongoRepository<Starboard, String> {

    Optional<Starboard> findByStarboardMessageId(long messageId);

}
