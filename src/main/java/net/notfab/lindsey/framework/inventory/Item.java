package net.notfab.lindsey.framework.inventory;

import net.notfab.lindsey.framework.inventory.enums.Type;

public interface Item {

    String getName();

    String getDescription();

    Type getType();

}
