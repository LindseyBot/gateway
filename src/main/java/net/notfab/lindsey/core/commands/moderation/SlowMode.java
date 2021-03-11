package net.notfab.lindsey.core.commands.moderation;

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
import net.notfab.lindsey.core.service.ModLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SlowMode implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private ModLogService logging;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("slowmode")
            .alias("slowoff")
            .permission("commands.slowmode", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            int seconds;
            try {
                seconds = Integer.parseInt(args[0]);
            } catch (IllegalArgumentException ex) {
                msg.send(channel, sender(member) + i18n.get(member, "core.not_number", args[0]));
                return false;
            }
            if (seconds > TextChannel.MAX_SLOWMODE || seconds < 0) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.slowmode.invalid", TextChannel.MAX_SLOWMODE));
                return false;
            }
            channel.getManager().setSlowmode(seconds)
                .reason(member.getUser().getName() + ": Slowmode")
                .queue(success -> this.logging.slowmode(channel, member.getIdLong(), seconds));
            msg.send(channel, sender(member) + i18n.get(member, "commands.mod.slowmode.active", seconds));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("slowmode")
            .text("commands.mod.slowmode.description")
            .usage("L!slowmode <timeInSeconds>")
            .url("https://github.com/LindseyBot/core/wiki/commands-slowmode")
            .permission("commands.slowmode")
            .addExample("L!slowmode 0")
            .addExample("L!slowmode 60");
        return HelpArticle.of(page);
    }

}
