package net.notfab.lindsey.core.commands.config;

import lombok.extern.slf4j.Slf4j;
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
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.shared.entities.profile.ServerProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Prefix implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private ProfileManager profiles;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("prefix")
            .module(Modules.CORE)
            .permission("commands.prefix", "permissions.command", false)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            ServerProfile profile = profiles.get(message.getGuild());
            profile.setPrefix(args[0]);
            profiles.save(profile);
            msg.send(channel, sender(member) + i18n.get(member, "commands.core.prefix.changed", args[0]));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("prefix")
            .text("commands.core.prefix.description")
            .usage("L!prefix <prefix>")
            .url("https://github.com/LindseyBot/core/wiki/commands-prefix")
            .permission("commands.prefix")
            .addExample("L!prefix !")
            .addExample("L!prefix >>");
        return HelpArticle.of(page);
    }

}
