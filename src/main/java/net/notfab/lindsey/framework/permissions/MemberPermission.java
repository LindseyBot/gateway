package net.notfab.lindsey.framework.permissions;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Data
@Entity
@IdClass(PermissionId.class)
public class MemberPermission {

    @Id
    private long role;

    @Id
    private String node;

    private boolean allowed;

}
