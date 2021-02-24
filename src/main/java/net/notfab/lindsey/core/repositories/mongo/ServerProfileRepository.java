package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServerProfileRepository extends MongoRepository<ServerProfile, String> {

}
