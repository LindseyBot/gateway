package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.core.framework.profile.ServerProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ServerProfileRepository extends MongoRepository<ServerProfile, String> {

}
