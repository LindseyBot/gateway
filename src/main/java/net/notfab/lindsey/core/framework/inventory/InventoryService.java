package net.notfab.lindsey.core.framework.inventory;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.inventory.enums.Items;
import net.notfab.lindsey.core.framework.inventory.enums.Type;
import net.notfab.lindsey.core.repositories.mongo.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class InventoryService {

    private final ItemRepository repository;

    public InventoryService(ItemRepository repository) {
        this.repository = repository;
    }

    public void has(Member member, Items model) {
        this.has(member.getUser(), model);
    }

    public void has(User user, Items model) {
        this.has(user.getIdLong(), model);
    }

    public boolean has(long user, Items model) {
        return this.has(user, model, 1);
    }

    public void has(Member member, Items model, int count) {
        this.has(member.getUser(), model, count);
    }

    public void has(User user, Items model, int count) {
        this.has(user.getIdLong(), model, count);
    }

    public boolean has(long user, Items model, int count) {
        Optional<Item> oStack = repository.findByUserAndModel(user, model);
        if (oStack.isEmpty()) {
            return false;
        }
        return oStack.get().getCount() >= count;
    }

    public void add(Member member, Items model, int count) {
        this.add(member.getUser(), model, count);
    }

    public void add(User user, Items model, int count) {
        this.add(user.getIdLong(), model, count);
    }

    public void add(long user, Items model, int count) {
        Optional<Item> oStack = repository.findByUserAndModel(user, model);
        Item stack;
        if (oStack.isEmpty()) {
            stack = new Item();
            stack.setId(user + ":" + model.name());
            stack.setModel(model);
            stack.setUser(user);
        } else {
            stack = oStack.get();
        }
        stack.setCount(stack.getCount() + count);
        repository.save(stack);
    }

    public void remove(Member member, Items model, int count) {
        this.remove(member.getUser(), model, count);
    }

    public void remove(User user, Items model, int count) {
        this.remove(user.getIdLong(), model, count);
    }

    public void remove(long user, Items model, int count) {
        Optional<Item> oStack = repository.findByUserAndModel(user, model);
        if (oStack.isEmpty()) {
            return;
        }
        Item stack = oStack.get();
        if (stack.getCount() - count < 0) {
            throw new IllegalArgumentException("commands.items.not_enough");
        }
        stack.setCount(stack.getCount() - count);
        if (stack.getCount() == 0) {
            repository.delete(stack);
        } else {
            repository.save(stack);
        }
    }

    public List<Item> findAllByType(long owner, Type type) {
        List<Items> models = Stream.of(Items.values())
            .filter(item -> item.getMetadata() != null)
            .filter(item -> item.getMetadata().getType() == type)
            .collect(Collectors.toList());
        if (models.isEmpty()) {
            return new ArrayList<>();
        }
        return repository.findAllByUserAndModelIn(owner, models);
    }

}
