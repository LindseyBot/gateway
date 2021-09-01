package net.notfab.lindsey.core.framework.command;

import net.dv8tion.jda.api.entities.Member;
import net.lindseybot.entities.interaction.commands.CommandMeta;

public abstract class Command {

    public CommandMeta getMetadata() {
        return null;
    }

    protected String getAsTag(Member member) {
        if (member == null) {
            return "Unknown?";
        }
        return member.getUser().getAsTag();
    }

}
