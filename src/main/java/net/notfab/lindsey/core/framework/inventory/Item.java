package net.notfab.lindsey.core.framework.inventory;

import lombok.Data;
import net.notfab.lindsey.core.framework.inventory.enums.Items;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document("Items")
public class Item {

    @Id
    private String id;
    private long user;
    private Items model;
    private int count;

}
