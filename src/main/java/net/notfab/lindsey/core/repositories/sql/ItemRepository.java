package net.notfab.lindsey.core.repositories.sql;

import net.notfab.lindsey.core.framework.inventory.InventoryId;
import net.notfab.lindsey.core.framework.inventory.ItemStack;
import net.notfab.lindsey.core.framework.inventory.enums.Items;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<ItemStack, InventoryId> {

    Optional<ItemStack> findByUserAndModel(long user, Items model);

}
