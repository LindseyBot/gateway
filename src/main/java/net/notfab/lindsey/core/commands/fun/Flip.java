package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.Bundle;
import net.notfab.lindsey.framework.command.Command;
import net.notfab.lindsey.framework.command.CommandDescriptor;
import net.notfab.lindsey.framework.command.Modules;
import net.notfab.lindsey.utils.Messenger;

import java.util.Random;

import static net.notfab.lindsey.framework.translate.Translator.translate;


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
            Messenger.send(channel, "**" + member.getEffectiveName() + "** " + translate("en", "core.commands.fun.flip.heads"));
        } else {
            Messenger.send(channel, "**" + member.getEffectiveName() + "** " + translate("en", "core.commands.fun.flip.tails"));
        }
        return false;
    }

}
