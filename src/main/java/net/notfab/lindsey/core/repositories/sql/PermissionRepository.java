package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.permissions.PermissionEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<PermissionEntry, Long> {

    List<PermissionEntry> findAllByGuild(long guild);

}
