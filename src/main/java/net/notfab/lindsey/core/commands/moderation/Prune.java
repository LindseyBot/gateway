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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Component
public class Prune implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("prune")
            .permission("commands.prune", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (!channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE)) {
            msg.send(channel, i18n.get(member, "permissions.bot", "Message Manage"));
            return false;
        }
        if (args.length == 0) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
            return true;
        }
        long twoWeeks = ((System.currentTimeMillis() - 1209600000) - 1420070400000L) << 22;
        List<Message> del = new ArrayList<>();
        List<Member> target = new ArrayList<>();
        int i;
        try {
            i = Integer.parseInt(args[0]) + 1;
        } catch (NumberFormatException e) {
            msg.send(channel, i18n.get(member, "core.not_number", args[0]));
            return false;
        }
        if (i < 3 || i > 100) {
            msg.send(channel, i18n.get(member, "commands.mod.prune.err"));
            return false;
        }
        List<Message> history = channel.getHistory().retrievePast(i).complete();

        if (args.length == 1) {
            history.forEach(m -> {
                if (Long.parseLong(m.getId()) > twoWeeks) {
                    del.add(m);
                }
            });
        } else {
            Stream.of(args).forEach(u -> {
                Member user = FinderUtil.findMember(u, message);
                if (user != null) {
                    target.add(user);
                }
            });
            history.stream().filter(m -> target.contains(m.getMember())).forEach(m -> {
                if (Long.parseLong(m.getId()) > twoWeeks) {
                    del.add(m);
                }
            });
        }
        int finalI = i;
        channel.deleteMessages(del)
            .flatMap(ignored -> channel.sendMessage(finalI - 1 + i18n.get(member, "commands.mod.prune.del")))
            .delay(5, TimeUnit.SECONDS)
            .flatMap(Message::delete)
            .queue();
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("prune")
            .text("commands.mod.prune.description")
            .usage("L!prune [member|id]")
            .permission("commands.prune")
            .addExample("L!prune 10")
            .addExample("L!prune 10 @lindsey");
        return HelpArticle.of(page);
    }

}
