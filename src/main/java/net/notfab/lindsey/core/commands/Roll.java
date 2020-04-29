package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.utils.Messenger;

import java.util.Random;

public class Roll implements Command {

    private final Random random = new Random();

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("roll")
                .module(Modules.FUN)
                .permission("commands.roll", "Permission to use the base command")
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        if (args.length == 0) {
            Messenger.send(channel, " rolled **" + random.nextInt(2000) + "**");
        } else {
            try {
                Messenger.send(channel, " rolled **" + random.nextInt(Integer.parseInt(args[0])) + "**");
            } catch (IllegalArgumentException ex) {
                Messenger.send(channel, "**" + args[0] + "** not a number");
            }
        }
        return false;
    }

}
