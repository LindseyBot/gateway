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
public class Roll implements Command {

    private final Random random = new Random();

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("roll")
            .module(Modules.FUN)
            .permission("commands.roll", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            msg.send(channel, i18n.get(member, "commands.fun.roll.roll") + "** " + random.nextInt(2000) + "**");
        } else {
            try {
                msg.send(channel, i18n.get(member, "commands.fun.roll.roll") + "** " + random.nextInt(Integer.parseInt(args[0])) + "**");
            } catch (IllegalArgumentException ex) {
                msg.send(channel, "**" + args[0] + "** " + i18n.get(member, "core.not_number",""));
            }
        }
        return false;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("roll")
            .text("commands.fun.roll.description")
            .usage("L!roll [max]")
            .permission("commands.roll")
            .addExample("L!roll")
            .addExample("L!roll 100");
        return HelpArticle.of(page);
    }

}
