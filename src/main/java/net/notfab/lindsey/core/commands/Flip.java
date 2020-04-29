package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;

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
    public boolean execute(Member member, TextChannel channel, String[] args) throws Exception {
        Random gem = new Random();
        if (gem.nextBoolean()) {
            channel.sendMessage("**" + member.getEffectiveName() + "** flipped a coin and got **Heads**.").queue();
        } else {
            channel.sendMessage("**" + member.getEffectiveName() + "** flipped a coin and got **Tails**.").queue();
        }
        return false;
    }

}
