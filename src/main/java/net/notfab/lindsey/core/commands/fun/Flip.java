package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.Bundle;
import net.notfab.lindsey.core.framework.command.Command;
import net.notfab.lindsey.core.framework.command.CommandDescriptor;
import net.notfab.lindsey.core.framework.command.Modules;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class Flip implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("flip")
            .module(Modules.FUN)
            .permission("commands.flip", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        Random gem = new Random();
        if (gem.nextBoolean()) {
            msg.send(channel, "**" + member.getEffectiveName() + "** " + i18n.get(member, "commands.fun.flip.heads"));
        } else {
            msg.send(channel, "**" + member.getEffectiveName() + "** " + i18n.get(member, "commands.fun.flip.tails"));
        }
        return false;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("flip")
            .text("commands.fun.flip.description")
            .usage("L!flip")
            .permission("commands.flip")
            .addExample("L!flip");
        return HelpArticle.of(page);
    }

}
