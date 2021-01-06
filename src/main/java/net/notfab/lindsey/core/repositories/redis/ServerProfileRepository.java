package net.notfab.lindsey.core.repositories.redis;

import net.notfab.lindsey.core.framework.profile.ServerProfile;
import org.springframework.data.keyvalue.repository.KeyValueRepository;

public interface ServerProfileRepository extends KeyValueRepository<ServerProfile, Long> {

}
