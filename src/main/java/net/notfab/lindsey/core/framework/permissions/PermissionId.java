package net.notfab.lindsey.core.framework.permissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermissionId implements Serializable {

    private long role;
    private String node;

}
