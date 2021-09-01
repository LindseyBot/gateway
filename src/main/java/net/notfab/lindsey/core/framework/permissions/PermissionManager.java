package net.notfab.lindsey.core.framework.permissions;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.lindseybot.enums.PermissionLevel;
import net.notfab.lindsey.core.repositories.sql.PermissionRepository;
import net.notfab.lindsey.shared.entities.permissions.PermissionEntry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PermissionManager {

    private final PermissionRepository repository;

    public PermissionManager(PermissionRepository repository) {
        this.repository = repository;
    }

    public boolean hasPermission(Member member, PermissionLevel target) {
        if (member.isOwner()) {
            return true;
        } else if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else if (isDeveloper(member)) {
            return true;
        }
        Map<Long, PermissionEntry> entries = this.repository.findAllByGuild(member.getGuild().getIdLong())
            .stream()
            .collect(Collectors.toMap(PermissionEntry::getTarget, e -> e));
        if (entries.containsKey(member.getIdLong())) {
            // Member overrides take priority.
            return this.has(entries.get(member.getIdLong()).getLevel(), target);
        } else if (target == PermissionLevel.EVERYONE && !entries.containsKey(member.getGuild().getIdLong())) {
            // Everyone role has no override.
            return true;
        }
        List<Role> roles = new ArrayList<>();
        roles.add(member.getGuild().getPublicRole());
        roles.addAll(member.getRoles());
        for (Role role : roles) {
            PermissionEntry entry = entries.get(role.getIdLong());
            if (entry == null) {
                continue;
            }
            PermissionLevel level = entry.getLevel();
            if (this.has(level, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean isDeveloper(Member member) {
        User user = member.getUser();
        return user.getIdLong() == 87166524837613568L;
    }

    private boolean has(PermissionLevel level, PermissionLevel target) {
        if (level.equals(target)) {
            return true;
        }
        return level.getWeight() >= target.getWeight();
    }

}
