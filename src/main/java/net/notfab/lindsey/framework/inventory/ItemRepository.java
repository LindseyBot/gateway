package net.notfab.lindsey.framework.inventory;

import net.notfab.lindsey.framework.inventory.enums.Items;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ItemRepository extends PagingAndSortingRepository<ItemStack, InventoryId> {

    Optional<ItemStack> findByUserAndModel(long user, Items model);

}
