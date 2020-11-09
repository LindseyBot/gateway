package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.core.framework.profile.MemberProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberProfileRepository extends MongoRepository<MemberProfile, String> {

}
