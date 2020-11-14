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
import net.notfab.lindsey.core.framework.profile.MemberProfile;
import net.notfab.lindsey.core.framework.profile.ProfileManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Strike implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private ProfileManager profiles;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("strike")
            .alias("warn")
            .permission("commands.strike", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length < 1) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            int count;
            Member target;
            String reason = i18n.get(member, "commands.mod.strike.noreason");
            try {
                count = Integer.parseInt(args[0]);
                target = FinderUtil.findMember(args[1], message);
                if (args.length > 2) {
                    reason = argsToString(args, 2);
                }
            } catch (IllegalArgumentException ex) {
                count = 1;
                target = FinderUtil.findMember(args[0], message);
                if (args.length > 1) {
                    reason = argsToString(args, 1);
                }
            }
            if (target == null) {
                msg.send(channel, sender(member) + i18n.get(member, "core.member_nf"));
                return false;
            }
            if (!member.canInteract(target) || target.isOwner()
                || target.hasPermission(Permission.ADMINISTRATOR)
                || target.getUser().isBot()
                || !member.hasPermission(Permission.BAN_MEMBERS)) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.strike.interact"));
                return false;
            }
            MemberProfile profile = profiles.get(target);
            profile.setStrikes(profile.getStrikes() + count);
            profiles.save(profile);

            Member finalTarget = target;
            String finalReason = reason;
            target.getUser().openPrivateChannel()
                .queue(dm -> dm.sendMessage(i18n.get(finalTarget, "commands.mod.strike.message", member.getGuild().getName(),
                    finalReason, profile.getStrikes())).queue());

            msg.send(channel, sender(member) + i18n.get(member, "commands.mod.strike.striked", profile.getStrikes(), count));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("strike")
            .text("commands.mod.strike.description")
            .usage("L!strike [count] <member> [reason]")
            .permission("commands.strike")
            .addExample("L!strike 2 @lindsey")
            .addExample("L!strike 2 @lindsey Not sending images")
            .addExample("L!strike @lindsey");
        return HelpArticle.of(page);
    }

}
