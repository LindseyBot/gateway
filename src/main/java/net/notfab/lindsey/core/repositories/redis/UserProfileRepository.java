package net.notfab.lindsey.core.repositories.redis;

import net.notfab.lindsey.core.framework.profile.UserProfile;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface UserProfileRepository extends KeyValueRepository<UserProfile, Long> {

}
