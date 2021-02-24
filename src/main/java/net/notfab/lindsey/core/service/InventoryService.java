package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.entities.ISnowflake;
import net.notfab.lindsey.shared.entities.items.ItemReference;
import net.notfab.lindsey.shared.repositories.sql.InventoryRepository;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public boolean has(ISnowflake snowflake, long itemId) {
        return this.has(snowflake, itemId, 1);
    }

    public boolean has(ISnowflake snowflake, long itemId, long cnt) {
        return this.has(snowflake.getIdLong(), itemId, cnt);
    }

    public boolean has(long owner, long itemId) {
        return this.has(owner, itemId, 1);
    }

    public boolean has(long owner, long itemId, long cnt) {
        ItemReference reference = this.get(owner, itemId);
        if (reference == null) {
            return false;
        }
        return reference.getCount() >= cnt;
    }

    public ItemReference get(long owner, Long itemId) {
        return this.repository.findByOwnerAndItemId(owner, itemId)
            .orElse(null);
    }

}
