package net.notfab.lindsey.core.commands.economy;

import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.framework.command.*;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import net.notfab.lindsey.framework.profile.ProfileManager;
import net.notfab.lindsey.framework.profile.UserProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Cookies implements Command {

    @Autowired
    private Translator i18n;

    @Autowired
    private Messenger msg;

    @Autowired
    private ProfileManager profiles;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("cookies")
            .alias("cookie")
            .module(Modules.ECONOMY)
            .permission("commands.cookies", "permissions.command")
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else if (args.length == 1) {
            Member target = FinderUtil.findMember(args[0], message);
            if (target == null) {
                UserProfile profile = profiles.get(member);
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.cookies.self", profile.getCookies()));
            } else {
                UserProfile profile = profiles.get(target);
                msg.send(channel, sender(member) + i18n.get(member, "commands.core.cookies.target", profile.getCookies()));
            }
        } else {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return false;
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("cookies")
            .text("commands.core.cookies.description")
            .usage("L!cookies <@user>")
            .permission("commands.cookies")
            .addExample("L!cookies")
            .addExample("L!cookies @Lindsey");
        return HelpArticle.of(page);
    }

}
