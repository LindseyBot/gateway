package net.notfab.lindsey.framework.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public interface Command {

    CommandDescriptor getInfo();

    boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception;

    default boolean hasPermission(Member member, String name) {
        if (member.isOwner()) {
            return true;
        }
        if (member.hasPermission(Permission.ADMINISTRATOR)) {
            return true;
        }
        // TODO: Profile check
        return true;
    }

    default boolean hasPermission(Member member, Permission... permission) {
        return member.hasPermission(permission);
    }

    default String argsToString(String[] args) {
        return String.join(" ", args);
    }

    default String sender(Member member) {
        return "**" + member.getEffectiveName() + "**";
    }

}
