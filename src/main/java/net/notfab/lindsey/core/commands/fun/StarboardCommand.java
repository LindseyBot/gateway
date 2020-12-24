package net.notfab.lindsey.core.commands.fun;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import net.notfab.lindsey.core.framework.profile.ServerProfile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StarboardCommand implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private ProfileManager profiles;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("starboard")
            .permission("commands.starboard", "permissions.command", false)
            .module(Modules.FUN)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            ServerProfile profile = profiles.get(member.getGuild());
            if (args[0].equalsIgnoreCase("OFF")) {
                profile.setStarboardChannelId(null);
                profiles.save(profile);
                msg.send(channel, sender(member) + i18n.get(member, "commands.fun.starboard.disabled"));
            } else {
                TextChannel target = FinderUtil.findTextChannel(argsToString(args, 0), channel.getGuild());
                if (target == null) {
                    msg.send(channel, sender(member) + i18n.get(member, "search.channel", argsToString(args, 0)));
                    return false;
                }
                profile.setStarboardChannelId(target.getIdLong());
                profiles.save(profile);
                msg.send(channel, sender(member) + i18n.get(member, "commands.fun.starboard.enabled", target.getAsMention()));
            }
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("starboard")
            .text("commands.fun.starboard.description")
            .usage("L!starboard <channel or OFF>")
            .url("https://github.com/LindseyBot/core/wiki/commands-starboard")
            .permission("commands.starboard")
            .addExample("L!starboard #starboard")
            .addExample("L!starboard off");
        return HelpArticle.of(page);
    }

}
