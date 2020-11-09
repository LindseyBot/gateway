package net.notfab.lindsey.core.framework.inventory;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.framework.inventory.enums.Items;
import net.notfab.lindsey.core.repositories.sql.ItemRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        Optional<ItemStack> oStack = repository.findByUserAndModel(user, model);
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
        Optional<ItemStack> oStack = repository.findByUserAndModel(user, model);
        ItemStack stack;
        if (oStack.isEmpty()) {
            stack = new ItemStack();
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
        Optional<ItemStack> oStack = repository.findByUserAndModel(user, model);
        if (oStack.isEmpty()) {
            return;
        }
        ItemStack stack = oStack.get();
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

}
