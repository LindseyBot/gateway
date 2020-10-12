package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.RestAction;
import net.notfab.lindsey.framework.command.*;
import net.notfab.lindsey.framework.command.help.HelpArticle;
import net.notfab.lindsey.framework.command.help.HelpPage;
import net.notfab.lindsey.framework.i18n.Messenger;
import net.notfab.lindsey.framework.i18n.Translator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class MultiBan implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("multiban")
            .alias("mban")
            .permission("commands.multiban", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            StringBuilder usersNotFound = new StringBuilder();
            StringBuilder usersAdmin = new StringBuilder();
            List<Member> usersToBan = new ArrayList<>();
            for (String user : args) {
                Member target = FinderUtil.findMember(user, message);
                if (target == null) {
                    usersNotFound.append(" ").append(user);
                    continue;
                }
                if (!member.canInteract(target) || target.isOwner()
                    || target.hasPermission(Permission.ADMINISTRATOR)
                    || target.getUser().isBot()
                    || !member.hasPermission(Permission.BAN_MEMBERS)) {
                    usersAdmin.append(" ").append(user);
                    continue;
                }
                usersToBan.add(target);
            }
            if (!usersNotFound.toString().equals("")) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.multiban.member_nf", usersNotFound.toString()));
            }
            if (!usersAdmin.toString().equals("")) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.ban.interact", usersAdmin.toString()));
            }
            if (!usersToBan.isEmpty()) {
                RestAction<?> action = null;
                String adminName = member.getUser().getName() + "#" + member.getUser().getDiscriminator();
                for (Member m : usersToBan) {
                    if (action == null) {
                        action = m.ban(7, i18n.get(member, "commands.mod.ban.audit", adminName));
                    } else {
                        action = action.flatMap(aVoid -> m.ban(7, i18n.get(member, "commands.mod.ban.audit", adminName)));
                    }
                }
                //noinspection ConstantConditions
                action.flatMap(aVoid -> channel.sendMessage(i18n.get(member, "commands.mod.multiban.ban", usersToBan.size())))
                    .delay(5, TimeUnit.SECONDS)
                    .flatMap(Message::delete)
                    .queue();
            }
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("multiban")
            .text("commands.mod.multiban.description")
            .usage("L!mban <member|id ...>")
            .permission("commands.multiban")
            .addExample("L!mban @Lindsey @Fabricio20 @Schneider");
        return HelpArticle.of(page);
    }

}
