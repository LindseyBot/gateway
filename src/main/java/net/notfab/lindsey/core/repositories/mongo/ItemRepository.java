package net.notfab.lindsey.core.repositories.mongo;

import net.notfab.lindsey.core.framework.inventory.Item;
import net.notfab.lindsey.core.framework.inventory.enums.Items;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ItemRepository extends MongoRepository<Item, String> {

    Optional<Item> findByUserAndModel(long user, Items model);

    List<Item> findAllByUserAndModelIn(long user, List<Items> models);

}
