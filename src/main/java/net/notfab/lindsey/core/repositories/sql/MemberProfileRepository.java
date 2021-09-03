package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.profile.MemberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {

    Optional<MemberProfile> findByUserAndGuild(long user, long guild);

}
