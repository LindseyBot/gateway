package net.notfab.lindsey.framework.command;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

public interface Command {

    boolean execute(Member member, TextChannel channel, String[] args) throws Exception;

}
