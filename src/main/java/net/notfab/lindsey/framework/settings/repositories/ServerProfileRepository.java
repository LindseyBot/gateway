package net.notfab.lindsey.framework.settings.repositories;

import net.notfab.lindsey.framework.settings.GuildProfile;
import org.springframework.data.repository.CrudRepository;

public interface ServerProfileRepository extends CrudRepository<GuildProfile, Long> {

}
