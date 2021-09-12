package net.notfab.lindsey.core.service;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import org.springframework.stereotype.Service;

@Service
public class PermissionsService {

    /**
     * Remove once Discord implements permissions v2.
     *
     * @param member  member.
     * @param command command.
     * @return If user has permission to use a command.
     */
    @Deprecated(forRemoval = true)
    public boolean hasPermission(Member member, String command) {
        if (member.isOwner()) {
            return true;
        } else if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        } else {
            return isDeveloper(member);
        }
    }

    private boolean isDeveloper(Member member) {
        User user = member.getUser();
        return user.getIdLong() == 87166524837613568L;
    }

}
