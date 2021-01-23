package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.core.framework.profile.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

}
