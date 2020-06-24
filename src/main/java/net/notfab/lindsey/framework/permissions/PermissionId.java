package net.notfab.lindsey.framework.permissions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class PermissionId implements Serializable {

    private long role;
    private String node;

}
