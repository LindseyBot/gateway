package net.notfab.lindsey.core.framework.inventory;

import net.notfab.lindsey.core.framework.inventory.enums.Type;

public interface Item {

    String getName();

    String getDescription();

    Type getType();

}
