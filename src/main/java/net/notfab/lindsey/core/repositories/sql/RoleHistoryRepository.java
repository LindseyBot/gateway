package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.profile.member.RoleHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleHistoryRepository extends JpaRepository<RoleHistory, Long> {

    Optional<RoleHistory> findByUserAndGuild(long user, long guild);

}
