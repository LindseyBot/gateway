package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.shared.entities.permissions.MemberPermission;
import net.notfab.lindsey.shared.entities.permissions.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<MemberPermission, PermissionId> {

    List<MemberPermission> findAllByRole(long role);

    void deleteAllByRole(long idLong);

    Optional<MemberPermission> findByRoleAndNode(long idLong, String name);

    void deleteByRoleAndNode(long idLong, String name);

}
