package net.notfab.lindsey.framework.profile.repositories;

import net.notfab.lindsey.framework.profile.GuildProfile;
import org.springframework.data.repository.CrudRepository;

public interface ServerProfileRepository extends CrudRepository<GuildProfile, Long> {

}
