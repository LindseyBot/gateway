package net.notfab.lindsey.framework.permissions;

import lombok.Getter;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.notfab.lindsey.framework.command.CommandManager;
import net.notfab.lindsey.framework.permissions.repositories.PermissionRepository;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PermissionManager {

    @Getter
    private static PermissionManager Instance;

    private final PermissionRepository repository;
    private final Map<String, Boolean> defaults = new HashMap<>();

    public PermissionManager(PermissionRepository repository) {
        Instance = this;
        this.repository = repository;
    }

    public boolean hasPermission(Member member, String... nodes) {
        if (member.isOwner()) {
            return true;
        }
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        // --
        List<Role> list = new ArrayList<>();
        list.add(member.getGuild().getPublicRole()); // Always add public role
        list.addAll(member.getRoles());
        list.sort(Comparator.comparing(Role::getPosition));
        // --
        Map<String, MemberPermission> perms = new HashMap<>();
        for (Role role : list) {
            for (MemberPermission memberPermission : repository.findAllByRole(role.getIdLong())) {
                perms.put(memberPermission.getNode(), memberPermission);
            }
        }
        for (String node : nodes) {
            node = node.toLowerCase();
            if (perms.containsKey(node)) {
                if (!perms.get(node).isAllowed()) {
                    return false;
                }
            } else if (!defaults.getOrDefault(node, false)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Initializes the default permission list.
     */
    public void init() {
        CommandManager.getInstance().getCommands()
            .stream()
            .map(cmd -> cmd.getInfo().getPermissions())
            .flatMap(Collection::stream)
            .forEach(perm -> this.defaults.put(perm.getName(), perm.isAllowed()));
    }

    public List<MemberPermission> list(Role role) {
        return repository.findAllByRole(role.getIdLong());
    }

    public void delete(Role role) {
        repository.deleteAllByRole(role.getIdLong());
    }

    public void delete(Role role, String name) {
        repository.deleteByRoleAndNode(role.getIdLong(), name);
    }

    public boolean exists(String name) {
        return this.defaults.containsKey(name);
    }

    public void set(Role role, String name, boolean status) {
        Optional<MemberPermission> optional = repository.findByRoleAndNode(role.getIdLong(), name);
        MemberPermission object;
        if (optional.isEmpty()) {
            object = new MemberPermission();
            object.setNode(name);
            object.setAllowed(status);
            object.setRole(role.getIdLong());
        } else {
            object = optional.get();
            object.setAllowed(status);
        }
        repository.save(object);
    }

}
