package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.shared.entities.profile.UserProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserProfileRepository extends MongoRepository<UserProfile, String> {

}
