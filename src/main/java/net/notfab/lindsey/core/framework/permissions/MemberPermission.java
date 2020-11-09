package net.notfab.lindsey.core.framework.permissions;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "permissions")
@IdClass(PermissionId.class)
public class MemberPermission {

    @Id
    private long role;

    @Id
    private String node;

    private boolean allowed;

}
