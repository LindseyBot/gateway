package net.notfab.lindsey.core.commands.moderation;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.notfab.lindsey.core.framework.command.*;
import net.notfab.lindsey.core.framework.command.help.HelpArticle;
import net.notfab.lindsey.core.framework.command.help.HelpPage;
import net.notfab.lindsey.core.framework.i18n.Messenger;
import net.notfab.lindsey.core.framework.i18n.Translator;
import net.notfab.lindsey.shared.entities.profile.member.Strike;
import net.notfab.lindsey.shared.repositories.sql.StrikeRepository;
import net.notfab.lindsey.shared.utils.Snowflake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Optional;

@Component
public class StrikesCmd implements Command {

    @Autowired
    private Messenger msg;

    @Autowired
    private Translator i18n;

    @Autowired
    private StrikeRepository repository;

    @Autowired
    private Snowflake snowflake;

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    public CommandDescriptor getInfo() {
        return new CommandDescriptor.Builder()
            .name("strikes")
            .permission("commands.strikes", "permissions.command", false)
            .module(Modules.MODERATION)
            .build();
    }

    @Override
    public boolean execute(Member member, TextChannel channel, String[] args, Message message, Bundle bundle) throws Exception {
        if (args.length < 1) {
            HelpArticle article = this.help(member);
            article.send(channel, member, args, msg, i18n);
        } else {
            Member target = FinderUtil.findMember(argsToString(args, 0), message);
            if (target == null) {
                msg.send(channel, sender(member) + i18n.get(member, "core.member_nf"));
                return false;
            }

            Optional<Strike> oStrike = this.repository
                .findTopByUserAndGuildOrderByIdDesc(target.getIdLong(), target.getGuild().getIdLong());
            if (oStrike.isEmpty()) {
                msg.send(channel, sender(member) + i18n.get(member, "commands.mod.strikes.empty", target.getEffectiveName()));
                return true;
            }
            Strike lastStrike = oStrike.get();

            int strikes = this.repository
                .sumByUserAndGuild(target.getIdLong(), member.getGuild().getIdLong());
            String date = this.sdf.format(Instant
                .ofEpochMilli(snowflake.parse(lastStrike.getId())[0]));
            String admin = "<@!" + lastStrike.getAdmin() + ">";

            msg.send(channel, sender(member) + i18n.get(member, "commands.mod.strikes.history",
                target.getEffectiveName(), date, admin, strikes));
        }
        return true;
    }

    @Override
    public HelpArticle help(Member member) {
        HelpPage page = new HelpPage("strikes")
            .text("commands.mod.strikes.description")
            .usage("L!strikes <member>")
            .url("https://github.com/LindseyBot/core/wiki/commands-strike#history")
            .permission("commands.strikes")
            .addExample("L!strikes @lindsey");
        return HelpArticle.of(page);
    }

}
