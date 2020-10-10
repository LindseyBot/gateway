package net.notfab.lindsey.framework.inventory;

import lombok.Data;
import net.notfab.lindsey.framework.inventory.enums.Items;

import javax.persistence.*;

@Data
@Entity
@Table(name = "items")
@IdClass(InventoryId.class)
public class ItemStack {

    @Id
    private long user;

    @Id
    @Enumerated(EnumType.STRING)
    private Items model;

    private int count = 0;

}
