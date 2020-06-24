package net.notfab.lindsey.core.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.*;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Help implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
                .name("help")
                .permission("commands.help", "permissions.command")
                .module(Modules.CORE)
                .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            String commandName = this.argsToString(args, 0);
            Command command = CommandManager.getInstance()
                    .findCommand(commandName.toLowerCase());
            if (command == null) {
                msg.send(channel, sender(member) + i18n.get(member, "core.help_nf"));
                return false;
            } else {
                HelpArticle article = command.help(member);
                article.send(channel, member, args, msg, i18n);
            }
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("help")
                .text("commands.core.help.description")
                .usage("L!help [command]")
                .permission("commands.help")
                .addExample("L!help help");
        return HelpArticle.of(page);
    }

}
