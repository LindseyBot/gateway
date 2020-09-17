package net.notfab.lindsey.framework.profile.repositories;

import net.notfab.lindsey.framework.profile.UserProfile;
import org.springframework.data.repository.CrudRepository;

public interface UserProfileRepository extends CrudRepository<UserProfile, Long> {

}
