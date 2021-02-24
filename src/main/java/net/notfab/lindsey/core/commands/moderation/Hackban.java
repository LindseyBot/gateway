package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.Permission;
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

import java.util.concurrent.TimeUnit;

@Component
public class Hackban implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("hackban")
            .permission("commands.ban", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            try {
                Long.parseLong(args[0]);
            } catch (IllegalArgumentException ex) {
                msg.send(channel, sender(member) + i18n.get(member, "core.not_number", args[0]));
                return false;
            }
            String reason = member.getUser().getName() + ": " + i18n.get(member, "commands.mod.ban.noreason");
            if (args.length > 1) {
                reason = member.getUser().getName() + ": " + argsToString(args, 1);
            }
            Member target = channel.getGuild().retrieveMemberById(args[0])
                .complete();
            if (target != null) {
                if (!member.canInteract(target) || target.isOwner()
                    || target.hasPermission(Permission.ADMINISTRATOR)
                    || target.getUser().isBot()
                    || !member.hasPermission(Permission.BAN_MEMBERS)) {
                    msg.send(channel, sender(member) + i18n.get(member, "commands.mod.ban.interact", target.getEffectiveName()));
                    return false;
                }
                target.ban(7, reason)
                    .flatMap(aVoid -> channel
                        .sendMessage(i18n.get(member, "commands.mod.ban.banned", target.getEffectiveName())))
                    .delay(5, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            } else {
                channel.getGuild().ban(args[0], 7, reason)
                    .flatMap(aVoid -> channel
                        .sendMessage(i18n.get(member, "commands.mod.ban.banned", args[0])))
                    .delay(5, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            }
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("hackban")
            .text("commands.mod.hackban.description")
            .usage("L!ban <id> [reason]")
            .permission("commands.ban")
            .addExample("L!ban 119482224713269248")
            .addExample("L!ban 119482224713269248 Not sending images");
        return HelpArticle.of(page);
    }

}
