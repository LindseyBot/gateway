package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SoftBan implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("softban")
            .alias("sban")
            .permission("commands.softban", "permissions.command", false)
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
            String reason = i18n.get(member, "commands.mod.softban.noreason");
            if (args.length > 1) {
                reason = argsToString(args, 1);
            }
            if (!member.canInteract(target) || target.isOwner()
                || target.hasPermission(Permission.ADMINISTRATOR)
                || target.getUser().isBot()
                || !member.hasPermission(Permission.BAN_MEMBERS)) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.softban.interact", target.getEffectiveName()));
                return false;
            }
            String finalReason = reason;
            target.getUser()
                .openPrivateChannel()
                .flatMap(dm -> dm.sendMessage(i18n.get(member, "commands.mod.softban.message", member.getGuild().getName(), finalReason)))
                .queue();
            target.ban(7, member.getUser().getName() + ": " + reason)
                .flatMap(aVoid -> channel
                    .sendMessage(i18n.get(member, "commands.mod.softban.banned", target.getEffectiveName())))
                .delay(5, TimeUnit.SECONDS)
                .flatMap(Message::delete)
                .and(member.getGuild().unban(target.getUser()))
                .queue();
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("softban")
            .text("commands.mod.softban.description")
            .usage("L!softban <member|id> [reason]")
            .url("https://github.com/LindseyBot/core/wiki/commands-softban")
            .permission("commands.softban")
            .addExample("L!softban @lindsey")
            .addExample("L!softban @lindsey Not sending images")
            .addExample("L!softban 119482224713269248");
        return HelpArticle.of(page);
    }

}
