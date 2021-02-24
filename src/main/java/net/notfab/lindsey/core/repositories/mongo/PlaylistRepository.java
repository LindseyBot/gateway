package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.shared.entities.playlist.PlayList;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface PlaylistRepository extends MongoRepository<PlayList, String> {

    long countAllByOwner(long owner);

    Optional<PlayList> findByOwnerAndNameLike(long owner, String name);

    List<PlayList> findAllByOwner(long owner);

}
