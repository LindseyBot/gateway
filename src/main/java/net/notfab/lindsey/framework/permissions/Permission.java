package net.notfab.lindsey.framework.permissions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permission {

    private String name;
    private String text;
    private boolean allowed;

}
