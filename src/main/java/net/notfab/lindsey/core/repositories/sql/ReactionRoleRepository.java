package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.ReactionRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReactionRoleRepository extends JpaRepository<ReactionRole, String> {

    List<ReactionRole> findAllByGuildId(long guildId);

}
