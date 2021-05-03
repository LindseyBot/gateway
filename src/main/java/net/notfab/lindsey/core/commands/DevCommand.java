package net.notfab.lindsey.core.commands;

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

@Component
public class DevCommand implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("dev")
            .module(Modules.CORE)
            .permission("commands.dev", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (member.getIdLong() != 87166524837613568L && member.getIdLong() != 119566649731842049L) {
            return false;
        }
        if (args.length == 0) {
            msg.send(channel, sender(member) + "Actions: reload");
            return false;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            Message reply = message.reply("Reloading languages...")
                .mentionRepliedUser(false)
                .complete();
            int total = this.i18n.reloadLanguages();
            reply.editMessage("Loaded " + total + " language files")
                .queue();
        } else {
            msg.send(channel, sender(member) + "Actions: reload");
            return false;
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("dev")
            .text("commands.core.dev.description")
            .usage("L!dev")
            .permission("commands.dev");
        return HelpArticle.of(page);
    }

}
