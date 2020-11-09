package net.notfab.lindsey.core.repositories.redis;

import net.notfab.lindsey.core.framework.profile.GuildProfile;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface ServerProfileRepository extends KeyValueRepository<GuildProfile, Long> {

}
