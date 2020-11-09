package net.notfab.lindsey.framework.profile.repositories;

import net.notfab.lindsey.framework.profile.MemberProfile;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MemberProfileRepository extends MongoRepository<MemberProfile, String> {



}
