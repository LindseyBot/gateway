package net.notfab.lindsey.framework.permissions.repositories;

import net.notfab.lindsey.framework.permissions.MemberPermission;
import net.notfab.lindsey.framework.permissions.PermissionId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PermissionRepository extends JpaRepository<MemberPermission, PermissionId> {

    List<MemberPermission> findAllByRole(long role);

}
