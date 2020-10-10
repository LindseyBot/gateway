package net.notfab.lindsey.framework.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.notfab.lindsey.framework.inventory.enums.Items;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryId implements Serializable {

    private long user;
    private Items model;

}
