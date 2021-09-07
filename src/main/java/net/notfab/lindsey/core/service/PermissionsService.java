package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.notfab.lindsey.core.repositories.sql.PermissionRepository;
import net.notfab.lindsey.shared.entities.permissions.PermissionEntry;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionsService {

    private final PermissionRepository repository;

    public PermissionsService(PermissionRepository repository) {
        this.repository = repository;
    }

    public boolean hasPermission(Member member, String command) {
        if (member.isOwner()) {
            return true;
        } else if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else if (isDeveloper(member)) {
            return true;
        }
        Optional<PermissionEntry> oPerms = this.repository.findByNameAndGuild(command, member.getGuild().getIdLong());
        if (oPerms.isEmpty()) {
            return false;
        }
        return member.getRoles().stream()
            .map(ISnowflake::getIdLong)
            .anyMatch(oPerms.get().getRoles()::contains);
    }

    private boolean isDeveloper(Member member) {
        User user = member.getUser();
        return user.getIdLong() == 87166524837613568L;
    }

}
