package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.permissions.PermissionEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissionRepository extends JpaRepository<PermissionEntry, Long> {

    Optional<PermissionEntry> findByNameAndGuild(String command, long guild);

}
