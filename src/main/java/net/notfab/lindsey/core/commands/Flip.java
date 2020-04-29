package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.utils.Messenger;

import java.util.Random;

public class Flip implements Command {

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("flip")
                .module(Modules.FUN)
                .permission("commands.flip", "Permission to use the base command")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        Random gem = new Random();
        if (gem.nextBoolean()) {
            Messenger.send(channel, "**" + member.getEffectiveName() + "** flipped a coin and got **Heads**.");
        } else {
            Messenger.send(channel, "**" + member.getEffectiveName() + "** flipped a coin and got **Tails**.");
        }
        return false;
    }

}
