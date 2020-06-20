package net.notfab.lindsey.framework.command;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;

public interface Command {

    CommandDescriptor getInfo();

    boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception;

    default HelpArticle help(Member member) {
        return HelpArticle.of(new HelpPage(getInfo().getName()).text("core.help_nf"));
    }

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

    default boolean hasPermission(Member member, Permission... permissions) {
        return member.hasPermission(permissions);
    }

    default String argsToString(String[] args, int index) {
        StringBuilder myString = new StringBuilder();
        for (int i = index; i < args.length; i++) {
            String arg = args[i] + " ";
            myString.append(arg);
        }
        if (myString.length() > 0) {
            myString = new StringBuilder(myString.substring(0, myString.length() - 1));
        }
        return myString.toString();
    }

    default String sender(Member member) {
        return "**" + member.getEffectiveName() + "**";
    }

}
