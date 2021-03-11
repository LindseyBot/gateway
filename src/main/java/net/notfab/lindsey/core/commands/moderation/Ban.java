package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.Utils;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.core.service.ModLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class Ban implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private ModLogService logging;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("ban")
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
            Member target = FinderUtil.findMember(args[0], message);
            if (target == null) {
                msg.send(channel, sender(member) + i18n.get(member, "core.member_nf"));
                return false;
            }
            String reason = member.getUser().getName() + ": " + i18n.get(member, "commands.mod.ban.noreason");
            if (args.length > 1) {
                reason = member.getUser().getName() + ": " + argsToString(args, 1);
            }
            if (!member.canInteract(target) || target.isOwner()
                || target.hasPermission(Permission.ADMINISTRATOR)
                || target.getUser().isBot()
                || !member.hasPermission(Permission.BAN_MEMBERS)) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.ban.interact", target.getEffectiveName()));
                return false;
            }
            String rawReason;
            if (args.length > 1) {
                rawReason = argsToString(args, 1);
            } else {
                rawReason = null;
            }
            target.ban(7, reason)
                .flatMap(aVoid -> {
                    this.logging.ban(target, member.getIdLong(), rawReason);
                    return channel.sendMessage(i18n.get(member, "commands.mod.ban.banned", target.getEffectiveName()));
                })
                .delay(5, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .queue(Utils.noop(), Utils.noop());
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("ban")
            .text("commands.mod.ban.description")
            .usage("L!ban <member|id> [reason]")
            .permission("commands.ban")
            .addExample("L!ban @lindsey")
            .addExample("L!ban @lindsey Not sending images")
            .addExample("L!ban 119482224713269248");
        return HelpArticle.of(page);
    }

}
