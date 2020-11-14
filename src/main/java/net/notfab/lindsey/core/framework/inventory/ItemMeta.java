package net.notfab.lindsey.core.framework.inventory;

import lombok.Data;
import net.notfab.lindsey.core.framework.inventory.enums.Type;

import java.util.List;

@Data
public class ItemMeta {

    private String name;
    private double price;
    private Type type;
    private String description;
    private List<String> authors;
    private List<String> tags;
    private String fontColor;

}
