package net.notfab.lindsey.core.commands.moderation;

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
import net.notfab.lindsey.shared.entities.profile.member.Strike;
import net.notfab.lindsey.shared.repositories.sql.StrikeRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StrikeCmd implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private StrikeRepository repository;

    @Autowired
    private Snowflake snowflake;

    @Autowired
    private ModLogService logging;

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
            String reason = null;
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
            if (!member.canInteract(target) || Utils.isDiscordModerator(target)) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.strike.interact"));
                return false;
            }
            Strike strike = new Strike();
            strike.setId(this.snowflake.next());
            strike.setAdmin(member.getIdLong());
            strike.setGuild(member.getGuild().getIdLong());
            strike.setUser(target.getIdLong());
            strike.setCount(count);
            strike.setReason(reason);
            this.repository.save(strike);

            this.logging.warn(target, member.getIdLong(), reason);

            if (reason == null) {
                reason = i18n.get(member, "commands.mod.strike.noreason");
            }

            String dmMsg = i18n.get(target, "commands.mod.strike.message", member.getGuild().getName(), reason);
            target.getUser().openPrivateChannel()
                .flatMap(dm -> dm.sendMessage(dmMsg))
                .queue(Utils.noop(), Utils.noop());

            int strikes = this.repository.sumByUserAndGuild(target.getIdLong(), member.getGuild().getIdLong());
            msg.send(channel, sender(member) + i18n.get(member, "commands.mod.strike.striked", strikes, count));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("strike")
            .text("commands.mod.strike.description")
            .usage("L!strike [count] <member> [reason]")
            .url("https://github.com/LindseyBot/core/wiki/commands-strike")
            .permission("commands.strike")
            .addExample("L!strike 2 @lindsey")
            .addExample("L!strike 2 @lindsey Not sending images")
            .addExample("L!strike @lindsey");
        return HelpArticle.of(page);
    }

}
